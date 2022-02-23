package com.ume.trimview.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.ume.adapter.DelegateAdapter
import com.ume.trimview.R
import com.ume.util.dp
import kotlin.math.max
import kotlin.math.min

class TrimView : FrameLayout {

    private var duration: Long = 0L
    private val leftHandle = AppCompatImageView(context)
    private val rightHandle = AppCompatImageView(context)
    private val seekHandle = AppCompatImageView(context)
    private val leftRange = AppCompatImageView(context)
    private val rightRange = AppCompatImageView(context)
    private val adapter = DelegateAdapter(context)
    private val frameSrc = FrameSource(adapter, resources.dp(50))
    private val list = RecyclerView(context)
    private val dragHelper = ViewDragHelper.create(this, 1000f, DragCallback())
    private val bg: MaterialShapeDrawable
    private val minDuration = 5000L
    private val maxDuration = 10000L
    private val maxWidth: Float
        get() = 1f * (width - paddingRight) - paddingLeft

    private val positionChangeListeners = mutableListOf<PositionChangeListener>(Listener())

    init {

        adapter.source = frameSrc
        adapter.addDelegate(FrameDelegate())
        list.layoutManager = object : LinearLayoutManager(context, HORIZONTAL, false) {
            override fun canScrollVertically(): Boolean = false
            override fun canScrollHorizontally(): Boolean = false
        }
        list.adapter = adapter

        dragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_RIGHT or ViewDragHelper.EDGE_LEFT)
        leftHandle.scaleType = ImageView.ScaleType.CENTER_CROP
        rightHandle.scaleType = ImageView.ScaleType.CENTER_CROP
        seekHandle.scaleType = ImageView.ScaleType.CENTER_CROP

        list.setBackgroundColor(Color.BLUE)
        addView(list, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        addView(leftRange, LayoutParams(WRAP_CONTENT, MATCH_PARENT))
        addView(rightRange, LayoutParams(WRAP_CONTENT, MATCH_PARENT)
            .apply { gravity = Gravity.RIGHT })
        addView(leftHandle, LayoutParams(WRAP_CONTENT, MATCH_PARENT)
            .apply { gravity = Gravity.LEFT })
        addView(rightHandle, LayoutParams(WRAP_CONTENT, MATCH_PARENT)
            .apply { gravity = Gravity.RIGHT })
        addView(seekHandle, LayoutParams(WRAP_CONTENT, MATCH_PARENT)
            .apply { gravity = Gravity.CENTER })

    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
        val a = context.obtainStyledAttributes(
            attr,
            R.styleable.TrimView,
            R.attr.trimViewStyle,
            R.style.TrimViewStyle
        )
        val leftD = a.getResourceId(R.styleable.TrimView_leftHandlerDrawable, 0)
        val rightD = a.getResourceId(R.styleable.TrimView_rightHandleDrawable, 0)
        val seekD = a.getResourceId(R.styleable.TrimView_seekHandleDrawable, 0)
        val strokeC = a.getColor(R.styleable.TrimView_strokeColor, Color.BLACK)
        val rangeBg = a.getColor(R.styleable.TrimView_rangeBackgroundColor, Color.BLACK)
        val strokeW = a.getDimension(R.styleable.TrimView_strokeWidth, 0f)

        ViewCompat.setBackground(leftRange, ColorDrawable(rangeBg)
            .apply { alpha = 200 })
        ViewCompat.setBackground(rightRange, ColorDrawable(rangeBg)
            .apply { alpha = 200 })

        setLeftHandleDrawableRes(leftD)
        setRightHandleDrawableRes(rightD)
        setSeekHandleDrawableRes(seekD)

        bg = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(
                if (background is ColorDrawable) (background as ColorDrawable).color
                else MaterialColors.getColor(this@TrimView, R.attr.colorSurface, Color.WHITE)
            )
            strokeColor = ColorStateList.valueOf(strokeC)
            strokeWidth = strokeW
        }
        ViewCompat.setBackground(this, bg)

        a.recycle()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        frameSrc.onUpdate(width)


    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        seekHandle.layout(
            leftHandle.right,
            seekHandle.top,
            leftHandle.right + seekHandle.width,
            seekHandle.bottom
        )
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return dragHelper.shouldInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        dragHelper.processTouchEvent(event)
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        dragHelper.continueSettling(true)
    }

    fun setUri(uri: Uri, duration: Long) {
        this.duration = duration
        frameSrc.uri = uri
        frameSrc.duration = duration
        if (ViewCompat.isLaidOut(this))
            frameSrc.onUpdate(width)
    }

    fun setLeftHandleDrawableRes(id: Int) {
        if (id != 0)
            leftHandle.setImageResource(id)
    }

    fun setRightHandleDrawableRes(id: Int) {
        if (id != 0)
            rightHandle.setImageResource(id)
    }

    fun setSeekHandleDrawableRes(id: Int) {
        if (id != 0)
            seekHandle.setImageResource(id)
    }

    fun addPositionListener(listener: PositionChangeListener) {
        positionChangeListeners += listener
    }

    fun removePositionListener(listener: PositionChangeListener) {
        positionChangeListeners -= listener
    }

    private fun dispatchPositionChanged(vararg handles: View) {
        for (handle in handles) {
            val pos = computeDuration(handle)
            for (listener in positionChangeListeners) {
                when (handle) {
                    leftHandle -> listener.onLeftPositionChanged(pos, handle)
                    rightHandle -> listener.onRightPositionChanged(pos, handle)
                    seekHandle -> listener.onSeekPositionChanged(pos, handle)
                }
            }
        }
    }

    private fun dispatchHandleReleased(vararg handles: View) {
        for (handle in handles) {
            for (listener in positionChangeListeners) {
                when (handle) {
                    leftHandle -> listener.onLeftHandleReleased(handle)
                    rightHandle -> listener.onRightHandleReleased(handle)
                    seekHandle -> listener.onSeekHandleReleased(handle)
                }
            }
        }
    }

    private fun computeDuration(handle: View): Long {

        return when (handle) {
            rightHandle -> (handle.right - paddingRight) / maxWidth * duration
            leftHandle -> (handle.left - paddingLeft) / maxWidth * duration
            else -> {
                when {
                    seekHandle.right >= rightHandle.left -> computeDuration(rightHandle)
                    seekHandle.left <= leftHandle.right -> computeDuration(leftHandle)
                    else -> (handle.left - paddingLeft) / maxWidth * duration
                }
            }
        }.toLong()
    }

    private fun computePosition(duration: Long): Int =
        ((maxWidth * duration) / this.duration).toInt()

    private inner class Listener : PositionChangeListener {
        override fun onRightPositionChanged(duration: Long, rightHandle: View) {
            super.onRightPositionChanged(duration, rightHandle)
            rightRange.left += (rightHandle.right - rightRange.left)
            val dx = rightHandle.left - seekHandle.right
            if (dx < 0) {
                ViewCompat.offsetLeftAndRight(seekHandle, dx)
                dispatchPositionChanged(seekHandle)
            }
        }

        override fun onLeftPositionChanged(duration: Long, leftHandle: View) {
            super.onLeftPositionChanged(duration, leftHandle)
            leftRange.right += (leftHandle.left - leftRange.right)
            val dx = leftHandle.right - seekHandle.left
            if (dx > 0) {
                ViewCompat.offsetLeftAndRight(seekHandle, dx)
                dispatchPositionChanged(seekHandle)
            }
        }
    }

    private inner class DragCallback : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return when (child) {
                leftHandle, rightHandle, seekHandle/*, rangeView */ -> true
                else -> false
            }
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            val minLen = computePosition(minDuration) + seekHandle.width
            val maxLen = computePosition(maxDuration)
            return when (child) {
                leftHandle -> {
                    val newLeft = min(
                        max(paddingLeft, left),
                        rightHandle.left - child.width - seekHandle.width
                    )

                    if (newLeft + child.width >= rightHandle.left - minLen
                        && rightHandle.right + dx >= width - paddingRight
                    ) {
                        child.left
                    } else
                        newLeft
                }
                rightHandle -> {
                    val newLeft = max(
                        leftHandle.right + seekHandle.width,
                        min(width - child.width - paddingRight, left)
                    )

                    if (leftHandle.left + dx <= paddingLeft && newLeft <= leftHandle.right + minLen)
                        child.left
                    else
                        newLeft
                }
                seekHandle -> min(max(leftHandle.right, left), rightHandle.left - child.width)
                else -> left
            }
        }

        override fun onViewPositionChanged(
            child: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            super.onViewPositionChanged(child, left, top, dx, dy)
            val minLen = computePosition(minDuration) + seekHandle.width
            when (child) {
                seekHandle -> dispatchPositionChanged(seekHandle)
                leftHandle -> {
                    if (child.right >= rightHandle.left - minLen
                        && rightHandle.right + dx < width - paddingRight
                    ) {
                        ViewCompat.offsetLeftAndRight(rightHandle, dx)
                        dispatchPositionChanged(rightHandle)
                    }
                    dispatchPositionChanged(leftHandle)
                }
                rightHandle -> {
                    if (child.left <= leftHandle.right + minLen && leftHandle.left + dx > paddingLeft) {
                        ViewCompat.offsetLeftAndRight(leftHandle, dx)
                        dispatchPositionChanged(leftHandle)
                    }

                    dispatchPositionChanged(rightHandle)
                }
            }
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return child.top
        }

        override fun onViewReleased(child: View, xvel: Float, yvel: Float) {
            super.onViewReleased(child, xvel, yvel)
            dispatchHandleReleased(child)
        }

        override fun getViewHorizontalDragRange(child: View): Int = 1

        override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
            super.onEdgeDragStarted(edgeFlags, pointerId)
            if (edgeFlags == ViewDragHelper.EDGE_LEFT && leftHandle.left == 0)
                dragHelper.captureChildView(leftHandle, pointerId)
            else if (edgeFlags == ViewDragHelper.EDGE_RIGHT && rightHandle.right == width)
                dragHelper.captureChildView(rightHandle, pointerId)
        }
    }

    interface PositionChangeListener {
        fun onLeftPositionChanged(duration: Long, leftHandle: View) {}
        fun onRightPositionChanged(duration: Long, rightHandle: View) {}
        fun onSeekPositionChanged(duration: Long, seekHandle: View) {}
        fun onLeftHandleReleased(handle: View) {}
        fun onRightHandleReleased(handle: View) {}
        fun onSeekHandleReleased(handle: View) {}
    }

}