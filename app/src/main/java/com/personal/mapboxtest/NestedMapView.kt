package com.personal.mapboxtest

import android.content.Context
import android.view.MotionEvent
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView

class NestedMapView(context: Context, options: MapInitOptions) : MapView(context, options) {
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            when (ev.action) {
                MotionEvent.ACTION_MOVE -> parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(
                    false
                )
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}