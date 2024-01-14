package com.ume.bottomsheet

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.MaterialShapeDrawable
import com.ume.util.dp
import com.ume.util.sdkAtLeast

class SheetBehavior(context: Context, attr: AttributeSet?) :
    BottomSheetBehavior<View>(context, attr) {

    var opacity: Float = 0f
    var dimColor: Int = 0
    var allowClickBehind = false

    private var interceptingTouch = false
    private var bounds = Rect()
    private var currentState = STATE_COLLAPSED
    private val drawBounds: Boolean

    private val bg by lazy {
        MaterialShapeDrawable().apply {
            strokeColor = ColorStateList.valueOf(Color.RED)
            fillColor = ColorStateList.valueOf(Color.TRANSPARENT)
            strokeWidth = context.resources.dp(2f)
        }
    }

    init {
        val a = context.obtainStyledAttributes(attr, R.styleable.SheetBevahior)
        drawBounds = a.getBoolean(R.styleable.SheetBevahior_behavior_drawClickBound, false)
        a.recycle()
        addBottomSheetCallback(Callback())
    }

    private fun onBoundaryChanged(child: View, parent: CoordinatorLayout) {
        child.getHitRect(bounds)

        if (sdkAtLeast(Build.VERSION_CODES.JELLY_BEAN_MR2) && drawBounds) {
            bg.bounds = bounds
            parent.overlay.clear()
            parent.overlay.add(bg)
        }
    }

    override fun setState(state: Int) {
        super.setState(state)
        currentState = state
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        super.onLayoutChild(parent, child, layoutDirection)

        onBoundaryChanged(child, parent)

        return true
    }

    override fun getScrimColor(parent: CoordinatorLayout, child: View): Int {
        return if (dimColor == 0) Color.TRANSPARENT else dimColor
    }

    override fun getScrimOpacity(parent: CoordinatorLayout, child: View): Float {
        return when (currentState) {
            STATE_EXPANDED -> opacity
            else -> super.getScrimOpacity(parent, child)
        }
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: View, event: MotionEvent): Boolean {
        if (interceptingTouch && isHideable && event.action == MotionEvent.ACTION_UP) {
            state = STATE_HIDDEN
        }
        return super.onTouchEvent(parent, child, event) || interceptingTouch
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: View,
        event: MotionEvent
    ): Boolean {
        interceptingTouch = currentState == STATE_EXPANDED
                && event.action == MotionEvent.ACTION_DOWN
                && !bounds.contains(event.x.toInt(), event.y.toInt())
                && !allowClickBehind

        return super.onInterceptTouchEvent(parent, child, event) || interceptingTouch
    }

    private inner class Callback : BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            onBoundaryChanged(bottomSheet, bottomSheet.parent as CoordinatorLayout)
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {

        }
    }
}