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
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.coroutines.*

class FragmentOption2 : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var geoApiContext: GeoApiContext
    private var markers = mutableListOf<Marker>()

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
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.d("FragmentOption2", "Map is ready")
        showSavedPlaces()
        showOriginalRoute()
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
        mMap.clear() // Esto elimina todos los marcadores y otras superposiciones del mapa
        Log.d("FragmentOption2", "All markers and overlays cleared")
    }

    private suspend fun addMarkerForPlace(place: Pair<String, String>) {
        withContext(Dispatchers.IO) {
            try {
                val results = GeocodingApi.geocode(geoApiContext, place.second).await()
                if (results.isNotEmpty()) {
                    val location = results[0].geometry.location
                    withContext(Dispatchers.Main) {
                        val latLng = LatLng(location.lat, location.lng)
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

    private fun showOriginalRoute() {
        // Definir los puntos de inicio, fin y waypoints de la ruta
        val start = com.google.maps.model.LatLng(27.84363467466449, -101.11492153163344)
        val end = com.google.maps.model.LatLng(27.84363467466449, -101.11492153163344)
        val waypoints = listOf(
            com.google.maps.model.LatLng(27.84705142713724, -101.1101262245768),
            com.google.maps.model.LatLng(27.85153832411702, -101.11403696042882),
            com.google.maps.model.LatLng(27.85524296150798, -101.11873915472707),
            com.google.maps.model.LatLng(27.845651806608924, -101.12157909385769),
            com.google.maps.model.LatLng(27.842687844693174, -101.11855292921028)
        )

        getRoute(start, end, waypoints)
    }

    private fun getRoute(start: com.google.maps.model.LatLng, end: com.google.maps.model.LatLng, waypoints: List<com.google.maps.model.LatLng>) {
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
                    drawRoute(result)
                }
            } catch (e: Exception) {
                Log.e("FragmentOption2", "Error getting route: ${e.message}")
            }
        }
    }

    private fun drawRoute(result: DirectionsResult) {
        val decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.encodedPath)

        val polylineOptions = PolylineOptions()
            .addAll(decodedPath)
            .color(Color.BLUE)
            .width(10f)

        mMap.addPolyline(polylineOptions)

        // Ajustar la cámara para mostrar toda la ruta
        val bounds = LatLngBounds.Builder()
        decodedPath.forEach { bounds.include(it) }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
        Log.d("FragmentOption2", "Route drawn")
    }

    override fun onResume() {
        super.onResume()
        if (::mMap.isInitialized) {
            showSavedPlaces()
        }
    }
}