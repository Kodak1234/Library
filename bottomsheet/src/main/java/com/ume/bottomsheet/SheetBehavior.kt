package com.ume.bottomsheet

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior

class SheetBehavior(context: Context, attr: AttributeSet?) :
    BottomSheetBehavior<View>(context, attr) {

    var opacity: Float = 0f
    var dimColor: Int = 0

    private var interceptingTouch = false
    private var bounds = Rect()

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        super.onLayoutChild(parent, child, layoutDirection)

        child.getHitRect(bounds)

        return true
    }

    override fun getScrimColor(parent: CoordinatorLayout, child: View): Int {
        return if (dimColor == 0) Color.TRANSPARENT else dimColor
    }

    override fun getScrimOpacity(parent: CoordinatorLayout, child: View): Float {
        return when (state) {
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
        interceptingTouch = state == STATE_EXPANDED
                && event.action == MotionEvent.ACTION_DOWN
                && !bounds.contains(event.x.toInt(), event.y.toInt())

        return super.onInterceptTouchEvent(parent, child, event) || interceptingTouch
    }
}