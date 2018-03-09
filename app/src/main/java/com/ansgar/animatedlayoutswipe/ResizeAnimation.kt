package com.ansgar.animatedlayoutswipe

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

/**
 * Created by Kirill on 3/9/18 12:00 PM
 */
class ResizeAnimation(private val view: View,
                      private val start: Float,
                      private val end: Float)
    : Animation() {

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        view.getLayoutParams().height = (start + (start - end) * interpolatedTime).toInt()
        view.requestLayout()
    }
}