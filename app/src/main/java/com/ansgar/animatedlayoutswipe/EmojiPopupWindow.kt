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

    private var topSwipeArea: Int = 0
    private var bottomSwipeArea: Int = 0

    private var animationSet: AnimationSet? = null
    private val childrenAnimatorsHash = HashMap<Int, Animator?>()

    private var childrenContainerLl: LinearLayout? = null
    private var backgroundView: View? = null
    private var defaultChildParams: LinearLayout.LayoutParams? = null
    private var defaultBackgroundViewParam: RelativeLayout.LayoutParams? = null
    private var selectedChildSize: Int = 0
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

        width = childrenContainerLl?.childCount?.let {
            defaultChildParams?.width?.times(it)
        } ?: 0
        height = defaultChildParams?.height?.times(3) ?: 0

        setTouchInterceptor(this)
    }

    /**
     * Define default children params and animation size [selectedChildSize] [smallChildSize]
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

        selectedChildSize = defaultChildParams?.width?.times(2) ?: 0
        smallChildSize = ((defaultChildParams?.width?.times(5) ?: 0) - selectedChildSize) / 4
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
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val x: Int = event.x.toInt() + offset
        val y: Int = event.y.toInt()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                topSwipeArea = 1
                bottomSwipeArea = contentView.height + contentView.height / 2

                touchXPos = x
                touchYPos = y

                onMoveMotionEvent(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                if (topSwipeArea == 0 || bottomSwipeArea == 0) {
                    topSwipeArea = -contentView.height
                    bottomSwipeArea = contentView.height / 2
                }
                if (Math.abs(x - touchXPos) >= 30 || Math.abs(y - touchYPos) >= 30) {
                    onMoveMotionEvent(x, y)
                    touchXPos = x
                    touchYPos = y
                }
            }
            MotionEvent.ACTION_UP -> {
                offset = 0
                if (currentView != null) {
                    currentView?.let {
                        onMenuItemSelectedListener?.onItemSelected(currentPosition, it)
                        pulseAnimation(it)
                    }
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
                for (i in (childrenContainerLl?.childCount?.minus(1) ?: 0) downTo 0) {
                    onCheckChildList(i, x, y)
                }
            } else {
                val size = childrenContainerLl?.childCount ?: 0
                for (i in 0 until size) {
                    onCheckChildList(i, x, y)
                }
            }
            childrenAnimatorsHash[0] = backgroundView?.let { getValueAnimator(it, smallChildSize) }
            prevX = x
        } else {
            val size = childrenContainerLl?.childCount ?: 0
            (0 until size)
                    .map { childrenContainerLl?.getChildAt(it) }
                    .forEachIndexed { index, view ->
                        if (view is LinearLayout) {
                            removeChildLabelAnimated(view.getChildAt(0))
                            childrenAnimatorsHash[index + 1] = getValueAnimator(view.getChildAt(1),
                                    selectedChildSize / 2, true)
                        } else if (view is ImageView) {
                            childrenAnimatorsHash[index + 1] = getValueAnimator(view,
                                    selectedChildSize / 2, true)
                        }
                    }
            currentView = null
            previousPosition = -1
            childrenAnimatorsHash[0] = backgroundView?.let { getValueAnimator(it, selectedChildSize / 2) }
        }

        if (currentPosition != previousPosition) {
            executeSelectedAnimation()
            // To avoid execute animation if this is not necessary
            if (checkInArea(x, y)) previousPosition = currentPosition
            else currentPosition = previousPosition
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
        val view = childrenContainerLl?.getChildAt(index)

        if (view?.left?.rangeTo(view.right)?.contains(x) == true && y in topSwipeArea..bottomSwipeArea) {
            currentView = view
            currentPosition = index
        }

        if (view is LinearLayout) {
            val imageView = view.getChildAt(1)

            if (index != currentPosition) {
                removeChildLabelAnimated(view.getChildAt(0))
                childrenAnimatorsHash[index + 1] = getValueAnimator(imageView, smallChildSize, true)
            } else {
                childrenAnimatorsHash[index + 1] = getValueAnimator(imageView, selectedChildSize, true)
            }
        } else if (view is ImageView) {
            if (index != currentPosition) {
                childrenAnimatorsHash[index + 1] = getValueAnimator(view, smallChildSize, true)
            } else {
                childrenAnimatorsHash[index + 1] = getValueAnimator(view, selectedChildSize, true)
            }
        }
    }

    /**
     * Display child [View] title if it defined in xml file if [currentView] instance of [LinearLayout]
     * otherwise skip
     */
    private fun showChildLabel() {
        if (currentView != null && currentView is LinearLayout) {
            val view = (currentView as LinearLayout).getChildAt(0)
            if (view.visibility != View.VISIBLE) {
                view.visibility = View.VISIBLE
            }
            executeAlphaAnimation(view, 0.0f, 1.0f, 200)
        }
    }

    /**
     * Remove specific label view with disapear animation
     * @param startAlpha start alpha value
     * @param endAlpha end alpha value
     * @param duration animation time in milliseconds
     */
    private fun removeChildLabelAnimated(view: View, startAlpha: Float = 1f, endAlpha: Float = 0f,
                                         duration: Long = 50) {
        executeAlphaAnimation(view, startAlpha, endAlpha, duration)
        if (view.visibility != View.GONE) {
            view.visibility = View.GONE
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
     * @param resizeWidth true if need change [LayoutParam.width]. Default false
     * @param duration The length of the animation, in milliseconds, of each of the child
     */
    private fun getValueAnimator(view: View, start: Int, resizeWidth: Boolean = false,
                                 duration: Long = 200): ValueAnimator {
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
     * @param view is the child which need to pulse animate
     * @param duration animation time in milliseconds. Default 100ms
     * @param repeatCount how many times pulse will repeat. Default 3 times
     */
    private fun pulseAnimation(view: View, duration: Long = 100, repeatCount: Int = 3) {
        val pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 0.6f),
                PropertyValuesHolder.ofFloat("scaleY", 0.6f))
        pulseAnimator.duration = duration
        pulseAnimator.repeatCount = repeatCount
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
                disappearViews()
                disappearBackground()
            }

        })
        pulseAnimator.start()
    }

    /**
     * Execute disappear animation for all defined views in [childrenContainerLl] except view
     * which was selected an for this view executed [pulseAnimation]
     */
    private fun disappearViews() {
        val size = childrenContainerLl?.childCount ?: 0
        for (i in 0 until size) {
            if (currentPosition != i) {
                val view = childrenContainerLl?.getChildAt(i)
                view?.let { executeAlphaAnimation(it, 1.0f, 0.0f, 100) }
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
     * Execute alpha animation for [View]
     * @param view is the view which need to animate
     * @param startAlpha start alpha value
     * @param endAlpha end alpha value
     * @param duration animation time in milliseconds
     */
    private fun executeAlphaAnimation(view: View, startAlpha: Float, endAlpha: Float, duration: Long) {
        val alphaAnimation = AlphaAnimation(startAlpha, endAlpha)
        alphaAnimation.duration = duration
        view.alpha = endAlpha
        view.startAnimation(alphaAnimation)
    }

    /**
     * Disposes of the popup window with animation
     */
    private fun dismissPopup() {
        val animateSlideUp = TranslateAnimation(0f, 0f, contentView.y,
                contentView.height.toFloat())
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
