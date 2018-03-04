package com.ansgar.animatedlayoutswipe

import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.PopupWindow

/**
 * Created by kirill on 4.3.18.
 */
class CustomPopupWindow(contentView: View?, width: Int, height: Int) : PopupWindow(contentView, width, height), View.OnTouchListener {

    init {
        contentView?.setOnTouchListener(this)
        setTouchInterceptor(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.i("Touch", "Touch")
            }
            MotionEvent.ACTION_MOVE -> {
                Log.i("Touch", "Event {${event.x}; ${event.y}}")
            }
            MotionEvent.ACTION_UP-> {
                Log.i("Touch", "Up")
            }
        }
        return true
    }

}