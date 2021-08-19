package com.personal.mapboxtest

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.*
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BaseMapFragment : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var onMapReady: (MapboxMap) -> Unit
    private lateinit var pointAnnotationManager: PointAnnotationManager

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            removeListeners()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mapView = NestedMapView(
            inflater.context,
            MapInitOptions(inflater.context)
        )
        mapView.scalebar.updateSettings {
            enabled = false
        }
        mapView.compass.updateSettings {
            enabled = false
        }
        return mapView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapboxMap = mapView.getMapboxMap()
        if (::onMapReady.isInitialized) {
            mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
                if (!::pointAnnotationManager.isInitialized) {
                    pointAnnotationManager =
                        mapView.annotations.createPointAnnotationManager(mapView)
                }
                onMapReady.invoke(mapboxMap)
            }
        }
        /*  locationPermissionHelper = LocationPermissionHelper(requireActivity())
          locationPermissionHelper.checkPermissions {
            mapView.getLocationComponentPlugin().apply {
              enabled = true
              pulsingEnabled = true
            }
            addListeners()
          }*/
    }

    fun getMapAsync(callback: (MapboxMap) -> Unit) = if (::mapboxMap.isInitialized) {
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
            if (!::pointAnnotationManager.isInitialized) {
                pointAnnotationManager =
                    mapView.annotations.createPointAnnotationManager(mapView)
            }
            callback.invoke(mapboxMap)
        }
    } else this.onMapReady = callback

    fun getMapView(): MapView {
        return mapView
    }

    fun addOnPointAnnotationClickListener(clickListener: OnPointAnnotationClickListener) {
        if (::pointAnnotationManager.isInitialized) {
            pointAnnotationManager.addClickListener(clickListener)
        }
    }

    fun setPoints(points: List<MapPoint>) {
        if (::pointAnnotationManager.isInitialized) {
            pointAnnotationManager.deleteAll()
            addPoints(points)
        }
    }

    private fun addPoints(points: List<MapPoint>) {
        if (::pointAnnotationManager.isInitialized) {
            lifecycleScope.launch(Dispatchers.IO) {
                val pointsAnnotationOptions = points.map { point ->
                    val futureTarget: FutureTarget<Bitmap> = Glide.with(requireContext())
                        .asBitmap()
                        .load(R.drawable.marker_arquitectos)
                        .submit(200, 200)
                    val bitmap = futureTarget.get()
                    PointAnnotationOptions().apply {
                        withPoint(com.mapbox.geojson.Point.fromLngLat(point.longitude,
                            point.latitude))
                        withIconImage(bitmap)
                        withIconSize(0.4)
                        /*withTextFont(
                          listOf(
                            "Arial Unicode MS Bold",
                            "Open Sans Regular"
                          )
                        )*/
                        withTextOffset(listOf(0.0, 3.0))
                        withTextSize(14.0)
                        withTextField(point.text)
                        withTextColor(Color.BLACK)
                        withTextHaloWidth(0.8)
                        withTextHaloColor(Color.WHITE)
                        withSymbolSortKey(100.0)
                    }
                }
                withContext(Dispatchers.Main) {
                    pointAnnotationManager.create(pointsAnnotationOptions)
                }
            }
        }
    }

    private fun addListeners() {
        mapView.location.apply {
            addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
            addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        }
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun removeListeners() {
        mapView.location.apply {
            removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
            removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        }
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        addListeners()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        removeListeners()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }


}