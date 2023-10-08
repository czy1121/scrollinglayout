package me.reezy.cosmo.scrollinglayout

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.Scroller
import androidx.annotation.IntDef
import androidx.core.view.*
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import me.reezy.cosmo.R
import kotlin.math.abs
import kotlin.math.max


class ScrollingLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ViewGroup(context, attrs, defStyleAttr), NestedScrollingParent3, NestedScrollingChild3, ScrollingView {


    private val mScrollOffset = IntArray(2)
    private val mScrollConsumed = IntArray(2)

    private var mNestedYOffset: Int = 0
    private var mLastMotionY: Int = 0
    private var mLastScrollerY: Int = 0
    private var mActivePointerId: Int = 0
    private var mIsBeingDragged: Boolean = false

    private var mScroller = Scroller(context)
    private var mTouchSlop: Int = 0
    private var mMinimumVelocity: Int = 0
    private var mMaximumVelocity: Int = 0


    init {
        ViewConfiguration.get(context).let {
            mTouchSlop = it.scaledTouchSlop
            mMinimumVelocity = it.scaledMinimumFlingVelocity
            mMaximumVelocity = it.scaledMaximumFlingVelocity
        }
    }

    //<editor-fold desc="触摸事件">
    private var velocityTracker: VelocityTracker? = null
        get() {
            if (field == null) {
                field = VelocityTracker.obtain()
            }
            return field
        }

    private var mDisallowIntercept = false
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.rawX.toInt()
                val y = ev.rawY.toInt()
                val rect = Rect()
                mDisallowIntercept = disallowInterceptViews.any {
                    it.getGlobalVisibleRect(rect)
                    rect.contains(x, y)
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                mDisallowIntercept = false
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (mDisallowIntercept) return false

        val action = ev.action
        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
            return true
        }
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> {
                val activePointerId = mActivePointerId
                if (activePointerId == INVALID_POINTER) {
                    return mIsBeingDragged
                }
                val pointerIndex = ev.findPointerIndex(activePointerId)
                if (pointerIndex == INVALID_POINTER) {
                    log("Invalid pointerId=$activePointerId in onInterceptTouchEvent")
                    return mIsBeingDragged
                }
                val y = ev.getY(pointerIndex).toInt()
                val yDiff = abs(y - mLastMotionY)
                if (yDiff > mTouchSlop && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL == 0) {
                    mIsBeingDragged = true
                    mLastMotionY = y
                    velocityTracker?.addMovement(ev)
                    mNestedYOffset = 0
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }

            MotionEvent.ACTION_DOWN -> {
                mLastMotionY = ev.y.toInt()
                mActivePointerId = ev.getPointerId(0)
                velocityTracker?.addMovement(ev)

                mScroller.computeScrollOffset()
                mIsBeingDragged = !mScroller.isFinished
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> endDrag()
            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)
        }
        return mIsBeingDragged
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val actionMasked = ev.actionMasked
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mNestedYOffset = 0
        }
        val vtev = MotionEvent.obtain(ev)
        vtev.offsetLocation(0f, mNestedYOffset.toFloat())
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (childCount == 0) {
                    return false
                }
                if (mIsBeingDragged) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                }

                if (!mScroller.isFinished) {
                    abortAnimatedScroll()
                }

                mLastMotionY = ev.y.toInt()
                mActivePointerId = ev.getPointerId(0)
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
            }

            MotionEvent.ACTION_MOVE -> kotlin.run {
                val activePointerIndex = ev.findPointerIndex(mActivePointerId)
                if (activePointerIndex == -1) {
                    log("Invalid pointerId=$mActivePointerId in onTouchEvent")
                    return@run
                }
                val y = ev.getY(activePointerIndex).toInt()
                var deltaY = mLastMotionY - y
                if (!mIsBeingDragged && abs(deltaY) > mTouchSlop) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    mIsBeingDragged = true
                    if (deltaY > 0) {
                        deltaY -= mTouchSlop
                    } else {
                        deltaY += mTouchSlop
                    }
                }
                if (mIsBeingDragged) {
                    // 父级先消费 nested-pre-scroll
                    if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset, ViewCompat.TYPE_TOUCH)) {
                        deltaY -= mScrollConsumed[1]
                        mNestedYOffset += mScrollOffset[1]
                    }

                    // 自己消费 self-scroll
                    mLastMotionY = y - mScrollOffset[1]

                    val scrolledY = scrollBy(deltaY)
                    val unconsumedY = deltaY - scrolledY

                    // 父级再次消费(剩下的) nested-scroll
                    mScrollConsumed[1] = 0
                    dispatchNestedScroll(0, scrolledY, 0, unconsumedY, mScrollOffset, ViewCompat.TYPE_TOUCH, mScrollConsumed)
                    mLastMotionY -= mScrollOffset[1]
                    mNestedYOffset += mScrollOffset[1]
                }
            }

            MotionEvent.ACTION_UP -> {
                tryFling()
                endDrag()
            }

            MotionEvent.ACTION_CANCEL -> {
                endDrag()
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = ev.actionIndex
                mLastMotionY = ev.getY(index).toInt()
                mActivePointerId = ev.getPointerId(index)
            }

            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId)).toInt()
            }
        }
        velocityTracker?.addMovement(vtev)
        vtev.recycle()

        return true
    }

    override fun computeScroll() {
        if (mScroller.isFinished) {
            return
        }
        mScroller.computeScrollOffset()
        val y = mScroller.currY
        var unconsumed: Int = y - mLastScrollerY
        mLastScrollerY = y

        // 父级先消费 nested-pre-scroll
        mScrollConsumed[1] = 0
        dispatchNestedPreScroll(0, unconsumed, mScrollConsumed, null, ViewCompat.TYPE_NON_TOUCH)
        unconsumed -= mScrollConsumed[1]
        if (unconsumed != 0) {

            // 自己消费 self-scroll
            val scrolledY = scrollBy(unconsumed)
            unconsumed -= scrolledY

            // 父级再次消费(剩下的) nested-scroll
            mScrollConsumed[1] = 0
            dispatchNestedScroll(0, scrolledY, 0, unconsumed, mScrollOffset, ViewCompat.TYPE_NON_TOUCH, mScrollConsumed)
            unconsumed -= mScrollConsumed[1]
        }
        if (!mScroller.isFinished) {
            ViewCompat.postInvalidateOnAnimation(this)
        } else {
            stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
        }
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.actionIndex
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new active pointer and adjust accordingly.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mLastMotionY = ev.getY(newPointerIndex).toInt()
            mActivePointerId = ev.getPointerId(newPointerIndex)
            velocityTracker?.clear()
        }
    }


    private fun startAnimatedScroll() {
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH)
        mLastScrollerY = scrollY
        ViewCompat.postInvalidateOnAnimation(this)
    }

    private fun abortAnimatedScroll() {
        mScroller.abortAnimation()
        stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
    }

    private fun tryFling() {
        if (childCount == 0) return
        val tracker = velocityTracker ?: return
        tracker.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
        val initialVelocity = tracker.getYVelocity(mActivePointerId).toInt()
        if (abs(initialVelocity) >= mMinimumVelocity) {
            mScroller.fling(scrollX, scrollY, 0, -initialVelocity, 0, 0, Int.MIN_VALUE, Int.MAX_VALUE)
            startAnimatedScroll()
        }
    }

    private fun endDrag() {
        mActivePointerId = INVALID_POINTER
        mIsBeingDragged = false
        velocityTracker?.recycle()
        velocityTracker = null
        stopNestedScroll(ViewCompat.TYPE_TOUCH)
    }
    //</editor-fold>

    //<editor-fold desc="滚动与吸顶">

    override fun scrollTo(x: Int, y: Int) {
        if (childCount > 0) {
            scrollBy(y - computeVerticalScrollOffset())
        }
    }

    override fun scrollBy(x: Int, y: Int) {
        if (childCount > 0) {
            scrollBy(y)
        }
    }

    private fun scrollBy(delta: Int): Int {

        var unconsumed = delta
        if (unconsumed > 0) {
            for ((child, scrollingView) in scrollingViews) {
                val offsetBottom = mMaxScrollY - scrollY
                val childTop = child.top
                val childOffset = childTop - scrollY

                if (childOffset < unconsumed || offsetBottom == 0) {
                    if (childOffset > 0 && offsetBottom > 0) {
                        scrollTo(childTop)
                        unconsumed -= childOffset
                    }

                    if (scrollingView.canScrollVertically(1)) {
                        scrollingView.scrollBy(0, unconsumed)
                        unconsumed = 0
                        break
                    }
                }
            }
        } else if (unconsumed < 0) {
            for ((child, scrollingView) in scrollingViews.reversed()) {
                val offsetTop = scrollY
                val childTop = child.top
                val childOffset = childTop - scrollY

                if (childOffset > unconsumed || offsetTop == 0) {
                    if (childOffset < 0 && offsetTop > 0) {
                        scrollTo(childTop)
                        unconsumed -= childOffset
                    }

                    if (scrollingView.canScrollVertically(-1)) {
                        scrollingView.scrollBy(0, unconsumed)
                        unconsumed = 0
                        break
                    }
                }
            }
        }
        if (unconsumed != 0) {
            val oldY = scrollY
            val newY = (oldY + unconsumed).coerceIn(0, mMaxScrollY)
            scrollTo(newY)
            unconsumed -= newY - oldY
        }

        return delta - unconsumed
    }

    private fun scrollTo(y: Int) {
        super.scrollTo(0, y)
        stickyTo(y)
    }

    private var mStickyHeight: Int = 0
    private fun stickyTo(y: Int) {
        mStickyHeight = 0
        stickyViews.forEachIndexed { index, view ->
            val lp = view.layoutParams as LayoutParams
            when (lp.sticky) {
                StickyMode.STICKY -> {
                    val nextTop = stickyViews.getOrNull(index + 1)?.top ?: (mMaxScrollY + view.height)
                    view.translationY = (y - view.top).coerceIn(0, nextTop - view.bottom).toFloat()
                }

                StickyMode.PERMANENT -> {
                    view.translationY = (y - view.top + lp.leadingHeight).coerceAtLeast(0).toFloat()
                }
            }
            if (view.translationY > 0f) {
                mStickyHeight = lp.leadingHeight + view.height
            }
        }
    }

    //</editor-fold>


    //<editor-fold desc="测量与布局">
    private var mChildrenHeight: Int = 0
    private var mMaxScrollY: Int = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mChildrenHeight = 0

        var maxWidth = 0
        var childState = 0
        val hPadding = paddingLeft + paddingRight
        val vPadding = paddingTop + paddingBottom

        var leadingHeight = 0
        var stickHeight = 0
        stickyViews.clear()

        forEach { child ->
            if (child.visibility == GONE) return@forEach

            val lp = child.layoutParams as LayoutParams

            val childWidthSpec = getChildMeasureSpec(widthMeasureSpec, hPadding + lp.leftMargin + lp.rightMargin, lp.width)
            val childHeightSpec = when {
                child is ViewPager2 && lp.height == ViewGroup.LayoutParams.MATCH_PARENT -> getChildMeasureSpec(heightMeasureSpec, vPadding + stickHeight, lp.height)
                child is ScrollingView || lp.height != ViewGroup.LayoutParams.WRAP_CONTENT -> getChildMeasureSpec(heightMeasureSpec, vPadding, lp.height)
                else -> getChildMeasureSpec(0, vPadding, lp.height)
            }
            child.measure(childWidthSpec, childHeightSpec)

            if (lp.sticky > 0) {
                if (lp.sticky == StickyMode.PERMANENT) {
                    lp.leadingHeight = leadingHeight
                    leadingHeight += child.measuredHeight
                    stickHeight = leadingHeight
                } else {
                    stickHeight = child.measuredHeight
                }
                child.translationZ = 1f
                stickyViews.add(child)
            }

            childState = combineMeasuredStates(childState, child.measuredState)

            mChildrenHeight += child.measuredHeight

            maxWidth = max(maxWidth, child.measuredWidth + lp.leftMargin + lp.rightMargin)
        }

        val widthSize = max(maxWidth + hPadding, suggestedMinimumWidth)
        val heightSize = max(mChildrenHeight + vPadding, suggestedMinimumHeight)

        val widthSizeAndState = resolveSizeAndState(widthSize, widthMeasureSpec, childState)
        val heightSizeAndState = resolveSizeAndState(heightSize, heightMeasureSpec, 0)

        setMeasuredDimension(widthSizeAndState, heightSizeAndState)
//        log("onMeasure => measuredHeight = $measuredHeight, childrenHeight = $mChildrenHeight")

    }


    private val stickyViews = mutableListOf<View>()
    private val scrollingViews = mutableListOf<Pair<View, View>>()
    private val disallowInterceptViews = mutableListOf<View>()


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        log("onLayout($changed, $l, $t, $r, $b)")

        val left: Int = super.getPaddingLeft()
        val right: Int = r - l - super.getPaddingRight()

        val childSpace: Int = right - left
        var childTop = super.getPaddingTop()

        scrollingViews.clear()
        disallowInterceptViews.clear()

        forEach { child ->
            if (child.visibility == GONE) return@forEach
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            val lp = child.layoutParams as LayoutParams


            val childLeft = when (lp.gravity) {
                Gravity.CENTER -> (left + (childSpace - childWidth) / 2 + lp.leftMargin) - lp.rightMargin
                Gravity.RIGHT -> right - childWidth - lp.rightMargin
                Gravity.LEFT -> left + lp.leftMargin
                else -> left + lp.leftMargin
            }

            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)

            if (lp.sticky == StickyMode.NONE) {
                if (lp.allowScrolling) {
                    val view = getScrollingView(child, lp)
                    if (view != null) {
//                        log("hasScrollingView => ${child.javaClass.simpleName}, ${view.javaClass.simpleName}")
                        scrollingViews.add(child to view)
                    }
                }
                if (!lp.allowIntercept) {
                    disallowInterceptViews.add(child)
                }
            }

            childTop += childHeight
        }


        mMaxScrollY = max(0, mChildrenHeight - computeVerticalScrollExtent())
    }


    private fun getScrollingView(child: View, lp: LayoutParams): View? {
        if (isScrollable(child)) {
            return child
        }
        if (child is ViewPager2) {
            val view = child.getCurrentView() ?: return null
            if (isScrollable(view)) {
                return view
            }
            if (lp.scrollingViewResolver != null) {
                return lp.scrollingViewResolver?.getScrollingView(child, view)
            }
            return view.findScrollingView(lp.scrollingViewId)
        }
        return child.findScrollingView(lp.scrollingViewId)
    }


    //</editor-fold>


    //<editor-fold desc="滚动接口 - ScrollingView">
    override fun computeHorizontalScrollOffset(): Int {
        return super.computeHorizontalScrollOffset()
    }

    override fun computeHorizontalScrollExtent(): Int {
        return super.computeHorizontalScrollExtent()
    }

    override fun computeHorizontalScrollRange(): Int {
        return super.computeHorizontalScrollRange()
    }


    override fun computeVerticalScrollExtent(): Int {
        return height - paddingTop - paddingBottom
    }

    override fun computeVerticalScrollOffset(): Int {
        return scrollingViews.sumOf { getVerticalScrollOffset(it.second) } + scrollY
    }

    override fun computeVerticalScrollRange(): Int {
        return scrollingViews.sumOf { getMaxVerticalScrollOffset(it.second) } + mChildrenHeight
    }

    //</editor-fold>

    //<editor-fold desc="嵌套滚动 - NestedScrollingParent">
    private val parentHelper = NestedScrollingParentHelper(this)
    private val consumed = intArrayOf(0, 0)

    override fun getNestedScrollAxes(): Int {
        return parentHelper.nestedScrollAxes
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH)
    }

    override fun onStopNestedScroll(child: View) {
        onStopNestedScroll(child, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        consumed[1] = 0
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH, consumed)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return false
    }
    //</editor-fold>

    //<editor-fold desc="嵌套滚动 - NestedScrollingParent2">

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0 && (child.layoutParams as LayoutParams).allowNestedScrolling
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type)
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        parentHelper.onStopNestedScroll(target, type)
        stopNestedScroll(type)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        consumed[1] = 0
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
//        log("pre-scroll => ${target.javaClass.simpleName}, $dy, ${consumed[1]}, $type")

        var unconsumed = dy

        if (unconsumed != 0) {
            mScrollConsumed[1] = 0
            childHelper.dispatchNestedPreScroll(0, unconsumed, mScrollConsumed, null, type)
            val delta = mScrollConsumed[1]
            consumed[1] += delta
            unconsumed -= delta
        }

        if (unconsumed != 0) {
            val delta = scrollBy(unconsumed)
            consumed[1] += delta
            unconsumed -= delta
        }
    }

    //</editor-fold>

    //<editor-fold desc="嵌套滚动 - NestedScrollingParent3">

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
//        log("scroll => $type, $dyConsumed, $dyUnconsumed, ${consumed[1]}")

        var unconsumed = dyUnconsumed
        var delta = 0

        if (unconsumed != 0) {
            delta = scrollBy(unconsumed)
            consumed[1] += delta
            unconsumed -= delta
        }

        if (unconsumed != 0) {
            childHelper.dispatchNestedScroll(0, delta, 0, unconsumed, null, type, consumed)
        }
    }
    //</editor-fold>

    //<editor-fold desc="嵌套滚动 - NestedScrollingChild">
    private val childHelper = NestedScrollingChildHelper(this).apply {
        isNestedScrollingEnabled = true
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return childHelper.isNestedScrollingEnabled
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    override fun hasNestedScrollingParent(): Boolean {
        return hasNestedScrollingParent(ViewCompat.TYPE_TOUCH)
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return startNestedScroll(axes, ViewCompat.TYPE_TOUCH)
    }

    override fun stopNestedScroll() {
        stopNestedScroll(ViewCompat.TYPE_TOUCH)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, ViewCompat.TYPE_TOUCH)
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        return dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, ViewCompat.TYPE_TOUCH)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }
    //</editor-fold>

    //<editor-fold desc="嵌套滚动 - NestedScrollingChild2">
    override fun hasNestedScrollingParent(type: Int): Boolean {
        return childHelper.hasNestedScrollingParent(type)
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return childHelper.startNestedScroll(axes, type)
    }

    override fun stopNestedScroll(type: Int) {
        childHelper.stopNestedScroll(type)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?, type: Int): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int): Boolean {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type)
    }
    //</editor-fold>

    //<editor-fold desc="嵌套滚动 - NestedScrollingChild3">
    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int, consumed: IntArray) {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed)
    }
    //</editor-fold>


    //<editor-fold desc="布局参数 - LayoutParams">
    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return LayoutParams(p)
    }

    class LayoutParams : MarginLayoutParams {

        var gravity: Int = 0
        var sticky: Int = 0

        var allowScrolling: Boolean = false
        var allowIntercept: Boolean = false
        var allowNestedScrolling: Boolean = false

        var scrollingViewId: Int = 0
        var scrollingViewResolver: ScrollingViewResolver? = null

        internal var leadingHeight: Int = 0

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.ScrollingLayout_Layout)
            gravity = a.getInt(R.styleable.ScrollingLayout_Layout_layout_gravity, 0)
            sticky = a.getInt(R.styleable.ScrollingLayout_Layout_layout_sticky, 0)
            allowScrolling = a.getBoolean(R.styleable.ScrollingLayout_Layout_layout_allowScrolling, true)
            allowIntercept = a.getBoolean(R.styleable.ScrollingLayout_Layout_layout_allowIntercept, true)
            allowNestedScrolling = a.getBoolean(R.styleable.ScrollingLayout_Layout_layout_allowNestedScrolling, true)

            scrollingViewId = a.getResourceId(R.styleable.ScrollingLayout_Layout_layout_scrollingViewId, 0)
            if (scrollingViewId == 0) {
                scrollingViewId = a.getInt(R.styleable.ScrollingLayout_Layout_layout_scrollingViewId, 0)
            }
            a.getString(R.styleable.ScrollingLayout_Layout_layout_scrollingViewResolver)?.let {
                kotlin.runCatching {
                    scrollingViewResolver = Class.forName(it).newInstance() as ScrollingViewResolver
                }
            }

            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(p: ViewGroup.LayoutParams?) : super(p)


    }

    @IntDef(Gravity.LEFT, Gravity.CENTER, Gravity.RIGHT)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Gravity {
        companion object {
            const val LEFT = 1
            const val RIGHT = 2
            const val CENTER = 3
        }
    }

    @IntDef(StickyMode.NONE, StickyMode.STICKY, StickyMode.PERMANENT)
    @Retention(AnnotationRetention.SOURCE)
    annotation class StickyMode {
        companion object {
            const val NONE = 0
            const val STICKY = 1
            const val PERMANENT = 2
        }
    }
    //</editor-fold>

    interface ScrollingViewResolver {
        fun getScrollingView(pager: ViewPager2, page: View): View?
    }

    companion object {


        private const val INVALID_POINTER = -1

        private fun log(message: String) {
            Log.e("OoO", message)
        }

        private val methodExtent by lazy {
            View::class.java.getDeclaredMethod("computeVerticalScrollExtent").apply {
                this.isAccessible = true
            }
        }
        private val methodOffset by lazy {
            View::class.java.getDeclaredMethod("computeVerticalScrollOffset").apply {
                this.isAccessible = true
            }
        }
        private val methodRange by lazy {
            View::class.java.getDeclaredMethod("computeVerticalScrollRange").apply {
                this.isAccessible = true
            }
        }

        private fun getVerticalScrollOffset(view: View): Int {
            if (view is ScrollingView) return view.computeVerticalScrollOffset()
            return methodOffset.invoke(view) as Int
        }

        private fun getMaxVerticalScrollOffset(view: View): Int {
            if (view is ScrollingView) return view.computeVerticalScrollRange() - view.computeVerticalScrollExtent()
            return methodRange.invoke(view) as Int - methodExtent.invoke(view) as Int
        }


        private fun ViewPager2.getCurrentView(): View? {
            if (adapter is FragmentStateAdapter) {
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        requestLayout()
                        post { unregisterOnPageChangeCallback(this) }
                    }
                })
                val rv = getChildAt(0) as RecyclerView
                val itemView = rv.findViewHolderForAdapterPosition(currentItem)?.itemView
                return if (itemView is FrameLayout && itemView.childCount > 0) itemView[0] else itemView
            }
            return null
        }

        private fun View.findScrollingView(scrollingViewId: Int): View? {
            if (scrollingViewId > 0) {
                val view = findViewById<View>(scrollingViewId)
                if (canScrollVertically(view)) {
                    return view
                }
                return view
            }

            if (scrollingViewId == 0 && this is ViewGroup) {
                children.firstOrNull { canScrollVertically(it) }?.let {
                    return it
                }
            }
            return null
        }

        private fun isScrollable(view: View): Boolean {
            return view is ScrollingView || view is ScrollView || view is WebView
        }

        private fun canScrollVertically(view: View): Boolean {
            return view.visibility != GONE && isScrollable(view) && (view.canScrollVertically(1) || view.canScrollVertically(-1))
        }
    }

}