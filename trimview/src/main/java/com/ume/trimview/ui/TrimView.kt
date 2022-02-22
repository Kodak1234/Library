package com.ume.trimview.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.ume.adapter.DelegateAdapter
import com.ume.trimview.R
import com.ume.util.dp
import kotlin.math.max
import kotlin.math.min

class TrimView : FrameLayout {

    private val leftHandle = AppCompatImageView(context)
    private val rightHandle = AppCompatImageView(context)
    private val seekHandle = AppCompatImageView(context)
    private val rangeView = AppCompatImageView(context)
    private val adapter = DelegateAdapter(context)
    private val frameSrc = FrameSource(adapter, resources.dp(50))
    private val list = RecyclerView(context)
    private val dragHelper = ViewDragHelper.create(this, 4f, DragCallback())
    private val bg: MaterialShapeDrawable

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
        addView(rangeView, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        addView(
            leftHandle,
            LayoutParams(WRAP_CONTENT, MATCH_PARENT).apply { gravity = Gravity.LEFT })

        addView(
            rightHandle,
            LayoutParams(WRAP_CONTENT, MATCH_PARENT).apply { gravity = Gravity.RIGHT })
        addView(
            seekHandle,
            LayoutParams(WRAP_CONTENT, MATCH_PARENT).apply { gravity = Gravity.CENTER })

    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
        val a = context.obtainStyledAttributes(attr, R.styleable.TrimView, 0, 0)
        val leftD = a.getResourceId(R.styleable.TrimView_leftHandlerDrawable, 0)
        val rightD = a.getResourceId(R.styleable.TrimView_rightHandleDrawable, 0)
        val seekD = a.getResourceId(R.styleable.TrimView_seekHandleDrawable, 0)
        val strokeC = a.getColor(R.styleable.TrimView_strokeColor, Color.BLACK)
        val rangeBg = a.getColor(R.styleable.TrimView_rangeBackgroundColor, Color.BLACK)
        val strokeW = a.getDimension(R.styleable.TrimView_strokeWidth, 0f)

        rangeView.setBackgroundColor(rangeBg)
        rangeView.alpha = 0.5f

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

        val padding = resources.dp(12)
        /*leftHandle.setOnTouchListener(TouchTargetHelper(leftHandle, padding))
        rightHandle.setOnTouchListener(TouchTargetHelper(rightHandle, padding))
        seekHandle.setOnTouchListener(TouchTargetHelper(seekHandle, padding))*/
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        rangeView.layout(leftHandle.right, rangeView.top, rightHandle.right, rangeView.bottom)
        /*seekHandle.layout(
            leftHandle.right,
            seekHandle.top,
            leftHandle.left + seekHandle.width,
            seekHandle.bottom
        )*/
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

    private inner class DragCallback : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return when (child) {
                leftHandle, rightHandle, seekHandle, rangeView -> true
                else -> false
            }
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return when (child) {
                leftHandle -> min(max(paddingLeft, left), rightHandle.left - child.width)
                rightHandle -> max(leftHandle.right, min(width - child.width - paddingRight, left))
                seekHandle -> min(max(leftHandle.right, left), rightHandle.left - child.width)
                rangeView -> {
                    min(
                        max(left, paddingLeft + leftHandle.width),
                        width - paddingRight - child.width
                    )
                }

                else -> left
            }
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return child.top
        }

        override fun onViewPositionChanged(
            child: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            super.onViewPositionChanged(child, left, top, dx, dy)
            when (child) {
                rangeView -> {
                    ViewCompat.offsetLeftAndRight(leftHandle, dx)
                    ViewCompat.offsetLeftAndRight(rightHandle, dx)
                    ViewCompat.offsetLeftAndRight(seekHandle, dx)
                }
                leftHandle -> {
                    rangeView.left += dx
                    /*if (seekHandle.left <= child.right && dx > 0)
                        seekHandle.left += dx*/
                }
                rightHandle -> {
                    rangeView.right += dx
                    if (seekHandle.right >= child.left && dx < 0)
                        seekHandle.left += dx
                }
            }
        }

        override fun getViewHorizontalDragRange(child: View): Int = 1

        override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
            super.onEdgeDragStarted(edgeFlags, pointerId)
            if (edgeFlags == ViewDragHelper.EDGE_LEFT && leftHandle.left == 0)
                dragHelper.captureChildView(leftHandle, pointerId)
            else if (edgeFlags == ViewDragHelper.EDGE_RIGHT && rightHandle.right == width)
                dragHelper.captureChildView(rightHandle, pointerId)
            Log.i("EdgeDrag", "onEdgeDragStarted: ")
        }
    }

    class TouchTargetHelper(
        view: View,
        padding: Int
    ) : OnTouchListener {
        private val delegate: TouchDelegate

        init {
            val rect = Rect()
            view.getHitRect(rect)
            rect.top -= padding;
            rect.left -= padding;
            rect.right += padding;
            rect.bottom += padding;
            delegate = TouchDelegate(rect, view)

            val c = ColorDrawable(Color.parseColor("#100000ff"))
            c.bounds = rect
            view.overlay.clear()
            view.overlay.add(c)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(p0: View?, p1: MotionEvent): Boolean {
            return delegate.onTouchEvent(p1) ?: false
        }
    }

}