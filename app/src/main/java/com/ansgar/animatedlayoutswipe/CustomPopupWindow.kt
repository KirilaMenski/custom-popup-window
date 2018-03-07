package com.ansgar.animatedlayoutswipe

import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout

/**
 * Created by kirill on 4.3.18.
 */
class CustomPopupWindow(contentView: RelativeLayout?, resourceId: Int, backgroundResId: Int) : PopupWindow(contentView), View.OnTouchListener {

    private var rightSwipeArea = 600
    private var topSwipeArea = -265
    private var leftSwipeArea = -25
    private var bottomSwipeArea = 135
    var offset = 0

    private var childLinearLayout: LinearLayout? = null
    private var currentView: View? = null
    private var currentPosition: Int = -1
    private var defaultParams: LinearLayout.LayoutParams? = null
    private var backgroundView: View? = null
    private var defaultBackgroundViewParam: RelativeLayout.LayoutParams? = null
    private var startX: Int = 0
    private var startY: Int = 0
    var onMenuItemSelectedListener: OnMenuItemSelectedListener? = null

    init {
        childLinearLayout = contentView?.findViewById(resourceId)
        backgroundView = contentView?.findViewById(backgroundResId)
        defaultBackgroundViewParam = backgroundView?.layoutParams as RelativeLayout.LayoutParams?

        defaultParams = (childLinearLayout as LinearLayout).getChildAt(0).layoutParams as LinearLayout.LayoutParams?
        width = defaultParams?.width!! * childLinearLayout!!.childCount
        height = defaultParams?.height!! * 2

        setTouchInterceptor(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val x: Int = event?.x!!.toInt() + offset
        val y: Int = event.y.toInt()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                topSwipeArea = 0
                bottomSwipeArea = 400

                if (!checkInArea(x, y)) {
                    dismiss()
                }
                onMoveMotionEvent(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                onMoveMotionEvent(x, y)
            }
            MotionEvent.ACTION_UP -> {
                offset = 0
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

    private fun onMoveMotionEvent(x: Int, y: Int) {
        val layout: LinearLayout = childLinearLayout as LinearLayout
        for (i in 0 until layout.childCount) {
            val view = layout.getChildAt(i)
            if (checkInArea(x, y)) {
                val width = defaultParams?.width!! * 2
                val height = defaultParams?.height!! * 2
                val smallSize = (contentView.width - width) / 4

//                if (x < view.right && x > view.left && y < bottomSwipeArea && y > topSwipeArea) {
                if (x in view.left..view.right && y in topSwipeArea..bottomSwipeArea) {
                    view.layoutParams = LinearLayout.LayoutParams(width, height)
                    currentView = view
                    currentPosition = i
                } else {
                    view.layoutParams = LinearLayout.LayoutParams(smallSize, smallSize)
                    val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, smallSize)
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    backgroundView?.layoutParams = params
                }
            } else {
                currentView = null
            }
        }

        if (currentView == null) {
            (0 until layout.childCount)
                    .map { layout.getChildAt(it) }
                    .forEach {
                        it.layoutParams = defaultParams
                        backgroundView?.layoutParams = defaultBackgroundViewParam
                    }
        }
    }

    private fun checkInArea(x: Int, y: Int): Boolean {
        return x in leftSwipeArea..rightSwipeArea && y in topSwipeArea..bottomSwipeArea
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