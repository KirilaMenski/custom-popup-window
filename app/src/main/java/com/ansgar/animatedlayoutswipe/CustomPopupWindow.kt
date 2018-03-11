package com.ansgar.animatedlayoutswipe

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.widget.*
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import java.util.ArrayList


/**
 * Created by kirill on 4.3.18.
 */
class CustomPopupWindow(contentView: RelativeLayout?, resourceId: Int, backgroundResId: Int) : PopupWindow(contentView), View.OnTouchListener {

    private val labels = arrayOf("Like", "Helpful", "Smart", "Funny", "Uplifting")
    private var rightSwipeArea = 500
    private var topSwipeArea = -265
    private var leftSwipeArea = -25
    private var bottomSwipeArea = 135
    var offset = 0
    private var animationSet: AnimationSet? = null
    private var isAnimationStarted: Boolean = false

    private var childLinearLayout: LinearLayout? = null
    private var currentView: View? = null
    private var currentPosition: Int = -1
    private var defaultChildParams: LinearLayout.LayoutParams? = null
    private var backgroundView: View? = null
    private var defaultBackgroundViewParam: RelativeLayout.LayoutParams? = null
    private var startX: Int = 0
    private var startY: Int = 0
    var onMenuItemSelectedListener: OnMenuItemSelectedListener? = null

    val arrayListObjectAnimators = ArrayList<Animator>()

    init {
        animationSet = AnimationSet(false)
        childLinearLayout = contentView?.findViewById(resourceId)
        backgroundView = contentView?.findViewById(backgroundResId)

        defaultBackgroundViewParam = backgroundView?.layoutParams as RelativeLayout.LayoutParams?
//        defaultChildParams = (childLinearLayout as LinearLayout).getChildAt(0).layoutParams as LinearLayout.LayoutParams?

        initDefaultChildParams()

        width = defaultChildParams?.width!! * childLinearLayout!!.childCount
        height = defaultChildParams?.height!! * 2

        setTouchInterceptor(this)
    }

    private fun initDefaultChildParams() {
        val children = (childLinearLayout as LinearLayout).getChildAt(0)
        if (children is LinearLayout) {
            (0 until children.childCount)
                    .filter { children.getChildAt(it) is ImageView }
                    .forEach { defaultChildParams = children.getChildAt(it).layoutParams as LinearLayout.LayoutParams }
        } else if (children is ImageView) {
            defaultChildParams = children.layoutParams as LinearLayout.LayoutParams
        }
    }

    private var touchXPos: Int = 0
    private var touchYPos: Int = 0
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val x: Int = event?.x!!.toInt() + offset
        val y: Int = event.y.toInt()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                topSwipeArea = 0
                bottomSwipeArea = 400

                touchXPos = x
                touchYPos = y

                if (!checkInArea(x, y)) dismissPopup()

                onMoveMotionEvent(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(x - touchXPos) >= 30 || Math.abs(y - touchYPos) >= 30) {
                    onMoveMotionEvent(x, y)
                    touchXPos = x
                    touchYPos = y
                }
            }
            MotionEvent.ACTION_UP -> {
                offset = 0
                if (currentView != null) {
                    onMenuItemSelectedListener?.itemSelected(currentPosition, currentView!!)
                    dismissPopup()
                }

                if (!checkInArea(x, y)) dismissPopup()
            }
        }
        return false
    }


    private val selectedChildWidth = defaultChildParams?.width!! * 2
    private val smallChildSize = (defaultChildParams?.width!! * 5 - selectedChildWidth) / 4
    private var prevX: Int = 0
    private fun onMoveMotionEvent(x: Int, y: Int) {
        if (checkInArea(x, y)) {
            val directRight = x - prevX > 0

            if (directRight) {
                for (i in childLinearLayout!!.childCount - 1 downTo 0) {
                    onCheckChildList(i, x, y)
                }
            } else {
                for (i in 0 until childLinearLayout!!.childCount) {
                    onCheckChildList(i, x, y)
                }
            }

            arrayListObjectAnimators.add(getValueAnimator(backgroundView!!, smallChildSize))
            prevX = x
        } else {
            (0 until childLinearLayout!!.childCount)
                    .map { childLinearLayout!!.getChildAt(it) }
                    .forEach {
                        arrayListObjectAnimators.add(getValueAnimator(it, selectedChildWidth / 2, true))
                    }
            currentView = null
            arrayListObjectAnimators.add(getValueAnimator(backgroundView!!, selectedChildWidth / 2))
        }

        if (!isAnimationStarted) {
            animateChildren(100)
        }
    }

    private fun onCheckChildList(index: Int, x: Int, y: Int) {
        val view = childLinearLayout!!.getChildAt(index)

        if (x in view.left..view.right && y in topSwipeArea..bottomSwipeArea) {
            currentView = view
            currentPosition = index
        }

        if (index != currentPosition) {
            arrayListObjectAnimators.add(getValueAnimator(view, smallChildSize, true))
        } else {
            arrayListObjectAnimators.add(getValueAnimator(view, selectedChildWidth, true))
        }
    }

    private fun getChildTextView(anchor: View, text: String): TextView {
        val labelTvParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        labelTvParams.leftMargin = anchor.left

        val childLabel = TextView(contentView.context)
        childLabel.text = text
        childLabel.setTextColor(Color.WHITE)
        childLabel.textSize = 10f
        childLabel.background = ContextCompat.getDrawable(contentView.context, R.drawable.background_v)
        childLabel.layoutParams = labelTvParams

        return childLabel
    }

    private fun showText(textView: TextView) {
        (contentView as RelativeLayout).addView(textView)
    }

    private fun removeText(textView: TextView) {
        (contentView as RelativeLayout).removeView(textView)
    }

    private fun checkInArea(x: Int, y: Int): Boolean = x in contentView.left..contentView.right
            && y in topSwipeArea..bottomSwipeArea

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
        startX = x
        startY = y
        onMenuItemSelectedListener?.menuOpened()
    }

    private fun getValueAnimator(view: View, start: Int, resizeWidth: Boolean = false): ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(view.measuredHeight, start)
        valueAnimator.addUpdateListener {
            val value = valueAnimator.animatedValue as Int
            val layoutParams = view.layoutParams
            if (resizeWidth) layoutParams.width = value
            layoutParams.height = value
            view.layoutParams = layoutParams
        }

        return valueAnimator
    }

    private fun animateChildren(duration: Long) {
        val objectAnimators = arrayListObjectAnimators.toTypedArray()
        val animSetXY = AnimatorSet()
        animSetXY.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                isAnimationStarted = false
                arrayListObjectAnimators.clear()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {
                isAnimationStarted = true
            }
        })
        animSetXY.playTogether(*objectAnimators)
        animSetXY.duration = duration
        animSetXY.start()
    }

    private fun dismissPopup() {
        val animateSlideUp = TranslateAnimation(0f, 0f, contentView.y, contentView.height.toFloat())
        animateSlideUp.duration = 300

        animateSlideUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                onMenuItemSelectedListener?.menuDismissed()
                dismiss()
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        contentView.startAnimation(animateSlideUp)
    }

    interface OnMenuItemSelectedListener {
        fun menuOpened()

        fun menuDismissed()

        fun itemSelected(position: Int, view: View)
    }

}