package com.ansgar.animatedlayoutswipe

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.LayoutInflater



/**
 * Created by kirill on 4.3.18.
 */
class CustomLayout(context: Context): ViewGroup(context) {

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.addView(inflater.inflate(R.layout.animated_layout, null))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            getChildAt(i).layout(l, t, r, b)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_BUTTON_PRESS -> {
                Log.i("Touch", "Touch")
            }
            MotionEvent.ACTION_MOVE -> {
                Log.i("Touch", "Event {${ev.x}; ${ev.y}}")
            }
            MotionEvent.ACTION_UP-> {
                Log.i("Touch", "Up")
            }
        }
        return true
    }

}