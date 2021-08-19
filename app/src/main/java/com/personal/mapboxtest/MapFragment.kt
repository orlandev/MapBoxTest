package com.personal.mapboxtest

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import kotlin.random.Random

class MapFragment : Fragment(), OnPointAnnotationClickListener {

    private val NAVIGATION_LINE_WIDTH = 6f
    private val NAVIGATION_LINE_OPACITY = .8f
    private val DRIVING_ROUTE_POLYLINE_LINE_LAYER_ID = "DRIVING_ROUTE_POLYLINE_LINE_LAYER_ID"
    private val DRIVING_ROUTE_POLYLINE_SOURCE_ID = "DRIVING_ROUTE_POLYLINE_SOURCE_ID"
    private val DRAW_SPEED_MILLISECONDS = 500

    private val mapboxDirectionsClient: MapboxDirections? = null
    private val handler: Handler = Handler()
    private val runnable: Runnable? = null


    private val mapFragment: BaseMapFragment by lazy {
        childFragmentManager.findFragmentById(R.id.map_fragment) as BaseMapFragment
    }


    data class cLocation(val lat: Double, val lon: Double)

    private val locations = listOf(
        cLocation(21.8055678, -79.985233),
        cLocation(21.805763, -79.9861212),
        cLocation(21.8062476, -79.9859264),
        cLocation(21.8065601, -79.9856226),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapFragment.getMapAsync { map ->
            mapFragment.addOnPointAnnotationClickListener(this@MapFragment)
            map.flyTo(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(locations[0].lon, locations[0].lat))
                    .zoom(15.0)
                    .build(), null)
            addMapsElements()
        }

    }


    fun addMapsElements() {

        val mapPoints = mutableListOf<MapPoint>()

        locations.forEach {
            val mapPoint = MapPoint(
                "Mapa Title - ${Random.nextInt(10)}",
                it.lat,
                it.lon,
                null
            )
            mapPoints.add(mapPoint)
        }

        mapFragment.setPoints(mapPoints)

    }

    override fun onAnnotationClick(annotation: PointAnnotation): Boolean {
        Toast.makeText(requireActivity(), "Has tocado un punto", Toast.LENGTH_SHORT).show()
        Log.d("TAG", "onAnnotationClick: TOCHED")
        return true
    }

}