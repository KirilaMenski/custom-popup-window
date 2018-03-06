package com.ansgar.animatedlayoutswipe

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.widget.RelativeLayout
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu

/**
 * Created by Kirill Kholdzeyeu on ${DATA} 3:32 PM
 */
class CustomLayout(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    private val children = ArrayList<View>()

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomLayout, 0, 0)
        post { createMainLayout() }
        typedArray.recycle()
    }

    /**
     * Create menu from [menuId]
     *
     * @return [Menu]
     */
    private val menu: Menu
        get() {
            val popupMenu = PopupMenu(context, null)
            val menuInflater = (context as Activity).menuInflater
            menuInflater.inflate(R.menu.test_menu, popupMenu.menu)

            return popupMenu.menu
        }

    private fun getBackgroundView(): View {
        val view = View(context)
        val width: Int = 100 * menu.size()
        val height: Int = 100
        val viewLayoutParams = RelativeLayout.LayoutParams(width, height)
        viewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        view.background = ContextCompat.getDrawable(context, R.drawable.background)
        view.layoutParams = viewLayoutParams

        return view
    }

    /**
     * Create linear layout where will be located children
     */
    private fun createLinearLayout(): LinearLayout {
        val width: Int = 100 * menu.size()
        val height: Int = 100
        val linearLayout = LinearLayout(context)
        val linearLayoutParams = RelativeLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT)
        linearLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        linearLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        linearLayout.setBackgroundColor(Color.RED)
        linearLayout.layoutParams = linearLayoutParams
        linearLayout.orientation = LinearLayout.HORIZONTAL
        Log.i("!!!!", "childCount: ${menu.size()}")
        for (i in 0 until menu.size()) {
            val childParams = LinearLayout.LayoutParams(height, height)

            val imageView = ImageView(context)
            imageView.id = menu.getItem(i).itemId
            imageView.background = menu.getItem(i).icon
            imageView.layoutParams = childParams

            linearLayout.addView(imageView)

        }

        return linearLayout
    }

    private fun createMainLayout() {
        val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT)

        addView(getBackgroundView())
        addView(createLinearLayout())
    }

}