package com.ansgar.animatedlayoutswipe

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.view.View
import android.widget.LinearLayout

/**
 * Created by Kirill Kholdzeyeu on ${DATA} 3:32 PM
 */
class CustomLayout(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    private val children = ArrayList<View>()

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomLayout, 0, 0)
        typedArray.recycle()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            removeView(view)
            children.add(view)
        }
        createMainLayout()
    }

    private fun getBackgroundView(): View {
        val view = View(context)
        val width: Int = getChildAt(0).width * childCount
        val height: Int = getChildAt(0).height
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
        val width: Int = getChildAt(0).width * childCount
        val height: Int = getChildAt(0).height
        val linearLayout = LinearLayout(context)
        val linearLayoutParams = RelativeLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT)
        linearLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        linearLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        linearLayout.setBackgroundColor(Color.RED)
        linearLayout.layoutParams = linearLayoutParams
        linearLayout.orientation = LinearLayout.HORIZONTAL

        (0 until children.size)
                .map { children[it] }
                .forEach { linearLayout.addView(it) }

        return linearLayout
    }

    private fun createMainLayout() {
        val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT)

        addView(getBackgroundView())
        addView(createLinearLayout())
    }

}