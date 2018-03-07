package com.ansgar.animatedlayoutswipe

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Created by Kirill Kholdzeyeu on 3:32 PM
 */
class CustomLayout : RelativeLayout {

    private var cont: Context? = null
    private var attrs: AttributeSet? = null
    private var imagesContainerLl: LinearLayout? = null

    private var layoutWidth: Int = 0
    private var layoutHeight: Int = 0
    private var labelTextSize: Float = 0f
    private var labelTextColor: Int = 0
    private var labelBackground: Int = 0
    private var labelPadding: Int = 0
    private var images = HashMap<String, ImageView>()
    private var imageSize: Int = 0
    private var imagesContainerBackgroundId: Int = -1
    private var layoutGravity: Int = -1

    constructor(cont: Context) : super(cont) {
        this.cont = cont
        createLayout()
    }

    constructor(cont: Context, attrs: AttributeSet) : super(cont, attrs) {
        this.cont = cont
        this.attrs = attrs
        createLayout()
    }

    private fun createLayout() {
        if (images.size != 0) {
            createImagesContainer()
        }

        if (imagesContainerBackgroundId != -1) {
            addView(getImagesContainerBackground())
        }

        if (imagesContainerLl != null) {
            addView(imagesContainerLl)
        }
    }

    private fun createImagesContainer() {
        imagesContainerLl = LinearLayout(context)
        val view = images[images.keys.toTypedArray()[0]]
        layoutWidth = view?.width!! * images.size
        layoutHeight = view.height * 2

        val imagesContainerLlParams: RelativeLayout.LayoutParams =
                RelativeLayout.LayoutParams(layoutWidth, layoutHeight)
        imagesContainerLlParams.addRule(gravity)

        imagesContainerLl?.layoutParams = imagesContainerLlParams

        for (key in images.keys) {
            val imageLl = LinearLayout(context)
            val imageLlParams: RelativeLayout.LayoutParams =
                    RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            imageLl.layoutParams = imageLlParams

            val imageView = images[key]
            val imageViewParams: RelativeLayout.LayoutParams = imageView?.layoutParams as LayoutParams
            imageViewParams.addRule(gravity)

            val textView = getImagesLabelTv(key)
            val textViewParams: RelativeLayout.LayoutParams
                    = RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            textViewParams.addRule(RelativeLayout.BELOW, imageView.id)

            imageLl.addView(imageView)
            imageLl.addView(textView)

            imagesContainerLl!!.addView(imageLl)
        }
    }

    private fun getImagesLabelTv(text: String): TextView {
        val textView = TextView(context)
        textView.text = text
        textView.textSize = labelTextSize
        if (labelTextColor == 0) labelTextColor = Color.BLACK
        textView.setTextColor(labelTextColor)

        return textView
    }

    private fun getImagesContainerBackground(): View {
        val view = View(context)
        val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(layoutWidth, layoutHeight / 2)
        params.addRule(gravity)
        view.setBackgroundResource(imagesContainerBackgroundId)

        return view
    }

    class Builder {
        private var context: Context? = null
        private var labelTextSize: Float = 0f
        private var labelTextColor: Int = 0
        private var labelBackground: Int = 0
        private var labelPadding: Int = 0
        private var images = HashMap<String, ImageView>()
        private var imageSize: Int = 0
        private var imagesContainerBackgroundId: Int = 0
        private var layoutGravity: Int = -1

        fun context(context: Context): Builder {
            this.context = context
            return this
        }

        fun labelTextSize(labelTextSize: Float): Builder {
            this.labelTextSize = labelTextSize
            return this
        }

        fun labelTextColor(labelTextColor: Int): Builder {
            this.labelTextColor = labelTextColor
            return this
        }

        fun labelBackground(labelBackground: Int): Builder {
            this.labelBackground = labelBackground
            return this
        }

        fun labelPadding(labelPadding: Int): Builder {
            this.labelPadding = labelPadding
            return this
        }

        fun images(images: HashMap<String, ImageView>): Builder {
            this.images = images
            return this
        }

        fun iamgesSize(imagesSize: Int): Builder {
            this.imageSize = imagesSize
            return this
        }

        fun imagesContainerBackground(imagesContainerBackground: Int): Builder {
            this.imagesContainerBackgroundId = imagesContainerBackground
            return this
        }

        fun layoutGravity(layoutGravity: Int): Builder {
            this.layoutGravity = layoutGravity
            return this
        }

        fun build(): CustomLayout {
            val customLayout = CustomLayout(context!!)
            customLayout.labelTextSize = labelTextSize
            customLayout.labelTextColor = labelTextColor
            customLayout.labelBackground = labelBackground
            customLayout.labelPadding = labelPadding
            customLayout.images = images
            customLayout.imageSize = imageSize
            customLayout.imagesContainerBackgroundId = imagesContainerBackgroundId
            customLayout.layoutGravity = layoutGravity
            return customLayout
        }

    }

}