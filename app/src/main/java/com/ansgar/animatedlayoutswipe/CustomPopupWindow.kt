package com.ansgar.animatedlayoutswipe

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow

/**
 * Created by kirill on 4.3.18.
 */
class CustomPopupWindow(contentView: LinearLayout?) : PopupWindow(contentView), View.OnTouchListener {

    private var currentView: View? = null
    private var currentPosition: Int = -1
    private var defaultParams: LinearLayout.LayoutParams? = null
    private var startX: Int = 0
    private var startY: Int = 0
    var onMenuItemSelectedListener: OnMenuItemSelectedListener? = null

    init {
        setTouchInterceptor(this)
        defaultParams = (contentView as LinearLayout).getChildAt(0).layoutParams as LinearLayout.LayoutParams?
        width = defaultParams?.width!! * contentView.childCount
        height = defaultParams?.height!! * 2
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val x: Int = event?.x!!.toInt()
        val y: Int = event.y.toInt()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!checkInArea(x, y)) {
                    dismiss()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                checkItem(x, y)
            }
            MotionEvent.ACTION_UP -> {
                if (currentView != null) {
                    onMenuItemSelectedListener?.itemSelected(currentPosition, currentView!!)
                    dismiss()
                }
                if (!checkInArea(x, y)) {
                    dismiss()
                }
            }
        }
        return false
    }

    private fun checkItem(x: Int, y: Int) {
        val layout: LinearLayout = contentView as LinearLayout
        for (i in 0 until layout.childCount) {
            val view = layout.getChildAt(i)
            if (checkInArea(x, y)) {
                val width = defaultParams?.width!! * 2
                val height = defaultParams?.height!! * 2
                val smallWidth = (contentView.width - width) / 4
                if (x < view.right && x > view.left && y < startY * 2 && y > -startY) {
                    view.layoutParams = LinearLayout.LayoutParams(width, height)
                    currentView = view
                    currentPosition = i
                } else {
                    view.layoutParams = LinearLayout.LayoutParams(smallWidth, smallWidth)
                }
            } else {
                currentView = null
            }
        }

        if (currentView == null) {
            (0 until layout.childCount)
                    .map { layout.getChildAt(it) }
                    .forEach { it.layoutParams = defaultParams }
        }
    }

    private fun checkInArea(x: Int, y: Int): Boolean {
        val rect = Rect(x, y, contentView.width, contentView.height)
        return rect.contains(x, y)
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
        startX = x
        startY = y
        onMenuItemSelectedListener?.menuOpened()
    }

    override fun dismiss() {
        super.dismiss()
        onMenuItemSelectedListener?.menuDismissed()
    }

    interface OnMenuItemSelectedListener {
        fun menuOpened()

        fun menuDismissed()

        fun itemSelected(position: Int, view: View)
    }

}