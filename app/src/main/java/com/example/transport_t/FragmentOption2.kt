package com.example.transport_t
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.transport_t.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentOption2 : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var geoApiContext: GeoApiContext

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

        // Definir los puntos de inicio, fin y waypoints de la ruta
        val start = com.google.maps.model.LatLng(27.84363467466449, -101.11492153163344) // Nueva York
        val end = com.google.maps.model.LatLng(27.84363467466449, -101.11492153163344)
        val waypoints = listOf(
            com.google.maps.model.LatLng(27.84705142713724, -101.1101262245768),
            com.google.maps.model.LatLng(27.85153832411702, -101.11403696042882),
            com.google.maps.model.LatLng(27.85524296150798, -101.11873915472707),
            com.google.maps.model.LatLng(27.845651806608924, -101.12157909385769), // Punto que hace que la ruta vuelva
            com.google.maps.model.LatLng(27.842687844693174, -101.11855292921028) // Repite un punto para volver a pasar
        )

        // Obtener y dibujar la ruta
        getRoute(start, end, waypoints)
    }

    private fun getRoute(start: com.google.maps.model.LatLng, end: com.google.maps.model.LatLng, waypoints: List<com.google.maps.model.LatLng>) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = DirectionsApi.newRequest(geoApiContext)
                    .origin(start)
                    .destination(end)
                    .waypoints(*waypoints.toTypedArray())
                    .mode(TravelMode.DRIVING)
                    .optimizeWaypoints(false) // Importante: no optimizar para mantener el orden
                    .await()

                withContext(Dispatchers.Main) {
                    drawRoute(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
    }
}