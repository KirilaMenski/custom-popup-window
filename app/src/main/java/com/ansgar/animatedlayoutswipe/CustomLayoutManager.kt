package com.ansgar.animatedlayoutswipe

import android.content.Context
import android.support.v7.widget.LinearLayoutManager

/**
 * Created by kirill on 4.3.18.
 */
class CustomLayoutManager(context: Context) : LinearLayoutManager(context) {

    var scrollEnabled: Boolean = true

    override fun canScrollVertically(): Boolean {
        return scrollEnabled
    }

}