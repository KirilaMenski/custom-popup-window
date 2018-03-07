package com.ansgar.animatedlayoutswipe

import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout

/**
 * Created by Kirill on 3/7/18 1:09 PM
 */
class CustomRelativePopupWindow(contentView: RelativeLayout?, resourceId: Int) : PopupWindow(contentView) {

    private var childPopup: CustomPopupWindow? = null

    init {
        val popupLinear: LinearLayout = contentView?.findViewById(resourceId)!!
//        childPopup = CustomPopupWindow(popupLinear)
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
    }

}