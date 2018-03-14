package com.ansgar.animatedlayoutswipe

import android.animation.*
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import java.util.ArrayList

/**
 * Created by kirill on 4.3.18.
 */
class EmojiPopupWindow(contentView: RelativeLayout?, resourceId: Int, backgroundResId: Int) :
        PopupWindow(contentView), View.OnTouchListener {

    private var topSwipeArea: Int = -265
    private var bottomSwipeArea: Int = 135

    private var animationSet: AnimationSet? = null
    private val childrenAnimatorsHash = HashMap<Int, Animator>()

    private var childrenContainerLl: LinearLayout? = null
    private var backgroundView: View? = null
    private var defaultChildParams: LinearLayout.LayoutParams? = null
    private var defaultBackgroundViewParam: RelativeLayout.LayoutParams? = null
    private var selectedChildWidth: Int = 0
    private var smallChildSize: Int = 0

    private var currentView: View? = null
    private var currentPosition: Int = -1
    private var previousPosition: Int = -1
    private var prevX: Int = 0

    var onMenuItemSelectedListener: OnMenuItemSelectedListener? = null

    /**
     * Because at the first time [PopupWindow] [onTouch] called from outside we need to define
     * offset of position where was called [PopupWindow] show.
     * When PopupWindow is displayed and after that touched again in swipe area then need to nullify
     * this value in [onTouch] after [MotionEvent.ACTION_UP] called at first time.
     */
    var offset: Int = 0

    init {
        animationSet = AnimationSet(false)
        childrenContainerLl = contentView?.findViewById(resourceId)
        backgroundView = contentView?.findViewById(backgroundResId)
        isOutsideTouchable = true

        initChildrenParams()

        width = defaultChildParams?.width!! * childrenContainerLl!!.childCount
        height = defaultChildParams?.height!! * 3

        setTouchInterceptor(this)
    }

    /**
     * Define default children params and animation size [selectedChildWidth] [smallChildSize]
     */
    private fun initChildrenParams() {
        defaultBackgroundViewParam = backgroundView?.layoutParams as RelativeLayout.LayoutParams?

        val children = (childrenContainerLl as LinearLayout).getChildAt(0)
        if (children is LinearLayout) {
            (0 until children.childCount)
                    .filter { children.getChildAt(it) is ImageView }
                    .forEach { defaultChildParams = children.getChildAt(it).layoutParams as LinearLayout.LayoutParams }
        } else if (children is ImageView) {
            defaultChildParams = children.layoutParams as LinearLayout.LayoutParams
        }

        selectedChildWidth = defaultChildParams?.width!! * 2
        smallChildSize = (defaultChildParams?.width!! * 5 - selectedChildWidth) / 4
    }

    /**
     * Unnecessary parameters which are responsible for the lack of choice in a small area (0, 0, 15, 15)
     * At the first time popup window touch event called from outside.
     * When PopupWindow is displayed and after that touched again in swipe area then touch
     * event called from this window. That's  why {x;y} is different for both mode.
     * To align swipe area for both mode need to change [topSwipeArea] and [bottomSwipeArea].
     */
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

                onMoveMotionEvent(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(x - touchXPos) >= 15 || Math.abs(y - touchYPos) >= 15) {
                    onMoveMotionEvent(x, y)
                    touchXPos = x
                    touchYPos = y
                }
            }
            MotionEvent.ACTION_UP -> {
                offset = 0
                if (currentView != null) {
                    onMenuItemSelectedListener?.onItemSelected(currentPosition, currentView!!)
                    pulseAnimation()
                }

                if (!checkInArea(x, y)) dismissPopup()
            }
            MotionEvent.ACTION_OUTSIDE -> {
                dismissPopup()
            }
        }
        return false
    }

    /**
     * Checked which view is selected in [childrenContainerLl]. If event is outside from swipe area
     * then return size of all views to default params
     * @param x is a finger position along x-axis
     * @param y is a finger position along y-axis
     */
    private fun onMoveMotionEvent(x: Int, y: Int) {
        if (checkInArea(x, y)) {
            //Define direction of the swipe
            val directRight = x - prevX > 0
            //If direction right then sorted out from end else from start
            if (directRight) {
                for (i in childrenContainerLl!!.childCount - 1 downTo 0) {
                    onCheckChildList(i, x, y)
                }
            } else {
                for (i in 0 until childrenContainerLl!!.childCount) {
                    onCheckChildList(i, x, y)
                }
            }

            childrenAnimatorsHash.put(0, getValueAnimator(backgroundView!!, smallChildSize))
            prevX = x
        } else {
            (0 until childrenContainerLl!!.childCount)
                    .map { childrenContainerLl!!.getChildAt(it) }
                    .forEachIndexed { index, view ->
                        if (view is LinearLayout) {
                            view.getChildAt(0).visibility = View.GONE
                            childrenAnimatorsHash.put(index + 1, getValueAnimator(view.getChildAt(1), selectedChildWidth / 2, true))
                        } else if (view is ImageView) {
                            childrenAnimatorsHash.put(index + 1, getValueAnimator(view, selectedChildWidth / 2, true))
                        }
                    }
            currentView = null
            previousPosition = -1
            childrenAnimatorsHash.put(0, getValueAnimator(backgroundView!!, selectedChildWidth / 2))
        }

        if (currentPosition != previousPosition) {
            executeSelectedAnimation()
            if (checkInArea(x, y)) previousPosition = currentPosition
        }
    }

    /**
     * Define which view is selected in [childrenContainerLl]. If selected view instance of
     * [LinearLayout] then loop this container to define children label([TextView])
     * and image view([ImageView]).
     * Otherwise add view in [childrenAnimators] to execute [executeSelectedAnimation]
     * @param x is a finger position along x-axis
     * @param y is a finger position along y-axis
     */
    private fun onCheckChildList(index: Int, x: Int, y: Int) {
        val view = childrenContainerLl!!.getChildAt(index)

        if (x in view.left..view.right && y in topSwipeArea..bottomSwipeArea) {
            currentView = view
            currentPosition = index
        }

        if (view is LinearLayout) {
            val imageView: ImageView = view.getChildAt(1) as ImageView
            val textView: TextView = view.getChildAt(0) as TextView

            if (index != currentPosition) {
                textView.visibility = View.GONE
                childrenAnimatorsHash.put(index + 1, getValueAnimator(imageView, smallChildSize, true))
            } else {
                childrenAnimatorsHash.put(index + 1, getValueAnimator(imageView, selectedChildWidth, true))
            }
        } else if (view is ImageView) {
            if (index != currentPosition) {
                childrenAnimatorsHash.put(index + 1, getValueAnimator(view, smallChildSize, true))
            } else {
                childrenAnimatorsHash.put(index + 1, getValueAnimator(view, selectedChildWidth, true))
            }
        }
    }

    /**
     * Display child [View] title if it defined in xml file if [currentView] instance of [LinearLayout]
     * otherwise skip
     */
    private fun showChildLabel() {
        if (currentView != null && currentView is LinearLayout) {
            (currentView as LinearLayout).getChildAt(0).visibility = View.VISIBLE
        }
    }

    /**
     * Remove child [View] title if it defined in xml file if [currentView] instance of [LinearLayout]
     * otherwise skip
     */
    private fun removeChildLabel() {
        if (currentView != null && currentView is LinearLayout) {
            (currentView as LinearLayout).getChildAt(0).visibility = View.GONE
        }
    }

    /**
     * Available swipe area
     * @param x is coordinate where finger located along the x-axis
     * @param y is coordinate where finger located along the y-axis
     */
    private fun checkInArea(x: Int, y: Int): Boolean = x in contentView.left..contentView.right
            && y in topSwipeArea..bottomSwipeArea

    /**
     * Display the [contentView] in a popup window at the specified position
     * @param parent a parent [contentView] view which displayed in popup window
     * @param gravity the gravity which controls the placement of the popup window
     * @param x a start position of popup window along the x-axis
     * @param y a start position of popup window along the y-axis
     */
    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
        onMenuItemSelectedListener?.onPopupOpened()
    }

    /**
     * Define [ValueAnimator] for specified view to play animation together view [AnimatorSet]
     * @param view is a specified child
     * @param start the initial size from which the animation is calculated
     * @param resizeWidth
     * @param duration The length of the animation, in milliseconds, of each of the child
     */
    private fun getValueAnimator(view: View, start: Int, resizeWidth: Boolean = false, duration: Long = 100): ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(view.measuredHeight, start)
        valueAnimator.addUpdateListener {
            val value = valueAnimator.animatedValue as Int
            val layoutParams = view.layoutParams
            if (resizeWidth) layoutParams.width = value
            layoutParams.height = value
            view.layoutParams = layoutParams
        }
        valueAnimator.duration = duration
        return valueAnimator
    }

    /**
     * Execute all loaded animation in [AnimatorSet] from [childrenAnimators]
     * animations of this AnimatorSet.
     */
    private fun executeSelectedAnimation() {
        val objectAnimators = ArrayList<Animator>(childrenAnimatorsHash.values).toTypedArray()
        val animSetXY = AnimatorSet()
        animSetXY.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                childrenAnimatorsHash.clear()
                showChildLabel()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })
        animSetXY.playTogether(*objectAnimators)
        animSetXY.start()
    }

    @SuppressLint("ObjectAnimatorBinding")
    /**
     * When specific view is selected then executed `pulse` animation which will repeated 3 times
     * until disappear
     */
    private fun pulseAnimation() {
        val pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(currentView,
                PropertyValuesHolder.ofFloat("scaleX", 0.6f),
                PropertyValuesHolder.ofFloat("scaleY", 0.6f))
        pulseAnimator.duration = 100
        pulseAnimator.repeatCount = 3
        pulseAnimator.repeatMode = ObjectAnimator.REVERSE
        pulseAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                dismissPopup()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {
                removeChildLabel()
                disappearChildView()
                disappearBackground()
            }

        })
        pulseAnimator.start()
    }

    /**
     * Execute disappear animation for all defined views in [childrenContainerLl]
     * when [pulseAnimation] is started
     */
    private fun disappearChildView() {
        for (i in 0 until childrenContainerLl?.childCount!!) {
            if (currentPosition != i) {
                val view = childrenContainerLl?.getChildAt(i)
                val alphaAnimation = AlphaAnimation(1.0f, 0.0f)
                alphaAnimation.duration = 100
                view?.alpha = 0f
                view?.startAnimation(alphaAnimation)
            }
        }
    }

    /**
     * Execute disappear animation for [backgroundView]
     */
    private fun disappearBackground() {
        val alphaAnimation = AlphaAnimation(1.0f, 0.0f)
        alphaAnimation.duration = 100
        backgroundView?.alpha = 0f
        backgroundView?.startAnimation(alphaAnimation)
    }

    /**
     * Disposes of the popup window with animation
     */
    private fun dismissPopup() {
        val animateSlideUp = TranslateAnimation(0f, 0f, contentView.y, contentView.height.toFloat())
        animateSlideUp.duration = 300

        animateSlideUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                onMenuItemSelectedListener?.onPopupDismissed()
                dismiss()
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        contentView.startAnimation(animateSlideUp)
    }

    /**
     * Callbacks of [EmojiPopupWindow].
     */
    interface OnMenuItemSelectedListener {
        fun onPopupOpened()

        fun onPopupDismissed()

        /**
         * [onItemSelected] called when specific view is selected.
         * @param position is a number of view in [childrenContainerLl].
         * @param view is a selected view
         */
        fun onItemSelected(position: Int, view: View)
    }

}
