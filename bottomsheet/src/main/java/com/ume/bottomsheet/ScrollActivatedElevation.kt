package com.ume.bottomsheet

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView

class ScrollActivatedElevation(
    radius: Float,
    private val elevation: Float,
    private val sheet: View,
    private val lift: View?,
    private val scroll: View?
) : NestedScrollView.OnScrollChangeListener, RecyclerView.OnScrollListener() {

    init {
        BackgroundUtil.setBackground(lift, radius)
        when (scroll) {
            is NestedScrollView -> scroll.setOnScrollChangeListener(this)
            is RecyclerView -> scroll.addOnScrollListener(this)
        }
    }

    override fun onScrollChange(
        v: NestedScrollView,
        scrollX: Int,
        scrollY: Int,
        oldScrollX: Int,
        oldScrollY: Int
    ) {
        onScrollChange(scrollY)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy >= 0)
            onScrollChange(dy)
        else {
            val child = recyclerView.getChildAt(0)
            val pos = recyclerView.getChildAdapterPosition(child)
            if (pos == 0 && child.top == 0)
                onScrollChange(0)
        }
    }

    fun setInterpolation(value: Float) {
        BackgroundUtil.getBackground(lift)?.interpolation = value
        BackgroundUtil.getBackground(sheet)?.interpolation = value
    }

    private fun onScrollChange(scrollY: Int) {
        if (scrollY > 0) {
            ViewCompat.setElevation(lift!!, elevation)
        } else if (scrollY == 0) {
            ViewCompat.setElevation(lift!!, 0f)
        }
    }
}