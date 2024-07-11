package com.example.transport_t

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.coroutines.*
import kotlin.random.Random

class FragmentOption2 : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var geoApiContext: GeoApiContext
    private var markers = mutableListOf<Marker>()
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_option2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        geoApiContext = GeoApiContext.Builder()
            .apiKey("AIzaSyCiom4P0R8ghymtiW4DM7uyeUhzNfMTelA")
            .build()

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.d("FragmentOption2", "Map is ready")
        showSavedPlaces()
        fetchRoutesFromFirebase()
    }

    private fun showSavedPlaces() {
        lifecycleScope.launch(Dispatchers.Main) {
            clearMarkers()
            val savedPlaces = withContext(Dispatchers.IO) {
                FragmentOption1.getSavedPlaces(requireContext())
            }
            Log.d("FragmentOption2", "Saved places: ${savedPlaces.size}")
            for (place in savedPlaces) {
                addMarkerForPlace(place)
            }
            if (savedPlaces.isNotEmpty()) {
                zoomToFitMarkers()
            } else {
                Log.d("FragmentOption2", "No saved places found")
            }
        }
    }

    private fun clearMarkers() {
        markers.forEach { it.remove() }
        markers.clear()
        mMap.clear() // This removes all markers and other overlays from the map
        Log.d("FragmentOption2", "All markers and overlays cleared")
    }

    private suspend fun addMarkerForPlace(place: Pair<String, String>) {
        withContext(Dispatchers.IO) {
            try {
                val results = GeocodingApi.geocode(geoApiContext, place.second).await()
                if (results.isNotEmpty()) {
                    val location = results[0].geometry.location
                    withContext(Dispatchers.Main) {
                        val latLng = LatLng(location.lat, location.lng).toGmsLatLng()
                        val marker = mMap.addMarker(MarkerOptions().position(latLng).title(place.first))
                        marker?.let {
                            markers.add(it)
                            Log.d("FragmentOption2", "Marker added for ${place.first} at ${latLng.latitude}, ${latLng.longitude}")
                        }
                    }
                } else {
                    Log.e("FragmentOption2", "No results found for ${place.second}")
                }
            } catch (e: Exception) {
                Log.e("FragmentOption2", "Error adding marker for ${place.first}: ${e.message}")
            }
        }
    }

    private fun zoomToFitMarkers() {
        if (markers.isEmpty()) {
            Log.d("FragmentOption2", "No markers to zoom to")
            return
        }
        val builder = LatLngBounds.Builder()
        markers.forEach { builder.include(it.position) }
        val bounds = builder.build()
        val padding = 100 // offset from edges of the map in pixels
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        mMap.animateCamera(cameraUpdate)
        Log.d("FragmentOption2", "Zoomed to fit markers")
    }

    private fun fetchRoutesFromFirebase() {
        database.child("routes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { routeSnapshot ->
                    try {
                        val start = routeSnapshot.child("start").getValue(LatLng::class.java)
                        val end = routeSnapshot.child("end").getValue(LatLng::class.java)
                        val waypoints = routeSnapshot.child("waypoints").children.map {
                            it.getValue(LatLng::class.java)
                        }.filterNotNull()

                        if (start != null && end != null && waypoints.isNotEmpty()) {
                            getRoute(start.toMapsLatLng(), end.toMapsLatLng(), waypoints.map { it.toMapsLatLng() }, routeSnapshot.key)
                        } else {
                            Log.e("FragmentOption2", "Invalid route data from Firebase for ${routeSnapshot.key}")
                        }
                    } catch (e: Exception) {
                        Log.e("FragmentOption2", "Error parsing route data for ${routeSnapshot.key}: ${e.message}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FragmentOption2", "Failed to fetch routes from Firebase: ${error.message}")
            }
        })
    }

    private fun getRoute(start: com.google.maps.model.LatLng, end: com.google.maps.model.LatLng, waypoints: List<com.google.maps.model.LatLng>, routeId: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = DirectionsApi.newRequest(geoApiContext)
                    .origin(start)
                    .destination(end)
                    .waypoints(*waypoints.toTypedArray())
                    .mode(TravelMode.DRIVING)
                    .optimizeWaypoints(false)
                    .await()

                withContext(Dispatchers.Main) {
                    drawRoute(result, routeId)
                }
            } catch (e: Exception) {
                Log.e("FragmentOption2", "Error getting route $routeId: ${e.message}")
            }
        }
    }

    private fun drawRoute(result: DirectionsResult, routeId: String?) {
        val decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.encodedPath)

        val color = getColorForRoute(routeId)
        val polylineOptions = PolylineOptions()
            .addAll(decodedPath)
            .color(color)
            .width(10f)

        mMap.addPolyline(polylineOptions)

        // Adjust the camera to show the entire route
        val bounds = LatLngBounds.Builder()
        decodedPath.forEach { bounds.include(it) }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
        Log.d("FragmentOption2", "Route $routeId drawn")
    }

    private fun getColorForRoute(routeId: String?): Int {
        return when (routeId) {
            "original" -> Color.BLUE
            else -> {
                // Generate a random color for other routes
                val rnd = Random
                Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::mMap.isInitialized) {
            showSavedPlaces()
        }
    }

    // Extension function to convert Firebase LatLng to Google Maps LatLng
    private fun LatLng.toMapsLatLng(): com.google.maps.model.LatLng {
        return com.google.maps.model.LatLng(this.latitude, this.longitude)
    }

    // Extension function to convert Google Maps LatLng to Android Maps LatLng
    private fun com.google.maps.model.LatLng.toGmsLatLng(): com.google.android.gms.maps.model.LatLng {
        return com.google.android.gms.maps.model.LatLng(this.lat, this.lng)
    }
}