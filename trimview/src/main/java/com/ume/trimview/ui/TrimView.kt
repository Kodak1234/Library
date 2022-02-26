package com.ume.trimview.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
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
import androidx.core.view.doOnLayout
import androidx.customview.widget.ViewDragHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.ume.adapter.DelegateAdapter
import com.ume.trimview.R
import com.ume.util.BitFlag
import com.ume.util.dp
import kotlin.math.max
import kotlin.math.min

class TrimView : FrameLayout {

    val minDuration: Long
    var maxDuration: Long
        private set
    val leftHandle = AppCompatImageView(context)
    val rightHandle = AppCompatImageView(context)
    val seekHandle = AppCompatImageView(context)
    private val leftRange = AppCompatImageView(context)
    private val rightRange = AppCompatImageView(context)
    private val rangeView = AppCompatImageView(context)
    private val adapter = DelegateAdapter(context)
    private val frameSrc = FrameSource(adapter, resources.dp(50))
    private val list = RecyclerView(context)
    private val dragHelper = ViewDragHelper.create(this, 1000f, DragCallback())
    private val bg: MaterialShapeDrawable
    private var maxLen = 0
    private var minLen = 0
    private var availableSpace = -1
    private val framesWindowSize: Int
        get() = if (availableSpace == -1) width else availableSpace

    private val positionChangeListeners = mutableListOf<PositionChangeListener>(Listener())
    private var savedState: SavedState? = null
    private val flag = BitFlag(0)

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

        addView(list, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        addView(leftRange, LayoutParams(WRAP_CONTENT, MATCH_PARENT))
        addView(rightRange, LayoutParams(WRAP_CONTENT, MATCH_PARENT)
            .apply { gravity = Gravity.RIGHT })
        addView(rangeView, LayoutParams(MATCH_PARENT, MATCH_PARENT))
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
        minDuration = a.getInteger(R.styleable.TrimView_minDuration, 0).toLong()
        maxDuration = a.getInteger(R.styleable.TrimView_maxDuration, 0).toLong()

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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST)
            availableSpace = MeasureSpec.getSize(widthMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        rangeView.layout(leftHandle.right, rangeView.top, rightHandle.left, rangeView.bottom)
        seekHandle.layout(
            leftHandle.right,
            seekHandle.top,
            leftHandle.right + seekHandle.width,
            seekHandle.bottom
        )

        updateWindowRange()

        savedState?.let { state ->
            val newRight = computePosition(state.endDuration)
            rightHandle.layout(
                newRight - rightHandle.width,
                rightHandle.top,
                newRight,
                rightHandle.bottom
            )

            val newLeft = computePosition(state.startDuration) + paddingLeft
            leftHandle.layout(
                newLeft,
                leftHandle.top,
                newLeft + leftHandle.width,
                leftHandle.bottom
            )

            dispatchPositionChanged(rightHandle, leftHandle)
        }

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

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            savedState = state
            state.uri?.let { setUri(it, state.duration) }
        } else
            super.onRestoreInstanceState(state)
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).apply {
            this.duration = frameSrc.duration
            startDuration = getStartDuration()
            endDuration = getEndDuration()
            uri = frameSrc.uri
        }
    }

    fun setUri(uri: Uri, duration: Long) {
        frameSrc.uri = uri
        frameSrc.duration = duration
        doOnLayout { frameSrc.onUpdate(framesWindowSize) }
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

    private fun updateWindowRange() {
        if (ViewCompat.isLaidOut(this) && frameSrc.duration > 0) {
            minLen = computePosition(minDuration) + seekHandle.width
            maxLen = computePosition(maxDuration)
            val dx = maxLen - rightHandle.right
            if (maxLen >= minLen && dx < 0) {
                ViewCompat.offsetLeftAndRight(rightHandle, dx)
                dispatchPositionChanged(rightHandle)
            } else {
                maxDuration = frameSrc.duration
                maxLen = computePosition(maxDuration)
            }
        }
    }

    private fun dispatchPositionChanged(vararg handles: View) {
        for (handle in handles) {
            for (listener in positionChangeListeners) {
                when (handle) {
                    leftHandle -> listener.onLeftPositionChanged()
                    rightHandle -> listener.onRightPositionChanged()
                    seekHandle -> listener.onSeekPositionChanged()
                }
            }
        }
    }

    private fun dispatchHandleReleased(vararg handles: View) {
        for (handle in handles) {
            for (listener in positionChangeListeners) {
                when (handle) {
                    leftHandle -> listener.onLeftHandleReleased()
                    rightHandle -> listener.onRightHandleReleased()
                    seekHandle -> listener.onSeekHandleReleased()
                    rangeView -> listener.onLeftHandleReleased()
                }
            }
        }
    }

    private fun maxWidth() = 1f * list.width

    private fun computeDuration(pos: Int): Long {
        return ((pos / maxWidth()) * frameSrc.duration).toLong()
    }

    private fun computePosition(d: Long): Int =
        ((maxWidth() * d) / frameSrc.duration).toInt()


    fun getSeekDuration() = computeDuration(seekHandle.left)

    fun getStartDuration() = computeDuration(leftHandle.left - paddingLeft)

    fun getEndDuration() = computeDuration(rightHandle.right)

    fun seekTo(positionMs: Long) {
        if (dragHelper.capturedView == null) {
            val baseDuration = getEndDuration() - getStartDuration()
            val baseLen = rightHandle.left - leftHandle.right - seekHandle.width

            var dx =
                ((baseLen * positionMs) / baseDuration - (seekHandle.left - leftHandle.right)).toInt()

            val newLeft = seekHandle.left + dx
            dx = when {
                newLeft < leftHandle.right -> seekHandle.left - leftHandle.right
                newLeft > rightHandle.left -> rightHandle.left - seekHandle.right
                else -> dx
            }

            ViewCompat.offsetLeftAndRight(seekHandle, dx)
        }
    }

    private inner class Listener : PositionChangeListener {
        override fun onRightPositionChanged() {
            super.onRightPositionChanged()
            rightRange.left += (rightHandle.right - rightRange.left)
            val dx = rightHandle.left - seekHandle.right
            if (dx < 0) {
                ViewCompat.offsetLeftAndRight(seekHandle, dx)
                dispatchPositionChanged(seekHandle)
            }

            if (flag.has(UPDATE_RANGE))
                rangeView.right += rightHandle.left - rangeView.right
        }

        override fun onLeftPositionChanged() {
            super.onLeftPositionChanged()
            leftRange.right += (leftHandle.left - leftRange.right)
            val dx = leftHandle.right - seekHandle.left
            if (dx > 0) {
                ViewCompat.offsetLeftAndRight(seekHandle, dx)
                dispatchPositionChanged(seekHandle)
            }

            if (flag.has(UPDATE_RANGE))
                rangeView.left += leftHandle.right - rangeView.left
        }
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
                leftHandle -> {
                    val newLeft = min(
                        max(paddingLeft, left),
                        rightHandle.left - child.width - seekHandle.width
                    )

                    val outsideMinLen = newLeft + child.width >= rightHandle.left - minLen
                            && rightHandle.right + dx >= width - paddingRight
                    if (outsideMinLen)
                        child.left
                    else
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
                rangeView -> {
                    min(
                        max(left, paddingLeft + leftHandle.width),
                        width - paddingRight - child.width - rightHandle.width
                    )
                }
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
            flag.add(UPDATE_RANGE)
            when (child) {
                rangeView -> {
                    flag.remove(UPDATE_RANGE)
                    ViewCompat.offsetLeftAndRight(leftHandle, dx)
                    ViewCompat.offsetLeftAndRight(seekHandle, dx)
                    ViewCompat.offsetLeftAndRight(rightHandle, dx)
                    dispatchPositionChanged(rightHandle, leftHandle, seekHandle)
                }
                seekHandle -> dispatchPositionChanged(seekHandle)
                leftHandle -> {
                    val outsideMinLen = child.right >= rightHandle.left - minLen
                            && rightHandle.right + dx < width - paddingRight
                    val outsideMaxLen = rightHandle.left - child.right >= maxLen && dx < 0
                    if (outsideMinLen || outsideMaxLen) {
                        ViewCompat.offsetLeftAndRight(rightHandle, dx)
                        dispatchPositionChanged(rightHandle)
                    }
                    dispatchPositionChanged(leftHandle)
                }
                rightHandle -> {
                    val outsideMinLen =
                        child.left <= leftHandle.right + minLen && leftHandle.left + dx > paddingLeft
                    val outsideMaxLen = child.left - leftHandle.right >= maxLen && dx > 0
                    if (outsideMinLen || outsideMaxLen) {
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

    private class SavedState : BaseSavedState {
        var startDuration = 0L
        var endDuration = 0L
        var uri: Uri? = null
        var duration = 0L

        @SuppressLint("ParcelClassLoader")
        constructor(parcel: Parcel) : super(parcel) {
            startDuration = parcel.readLong()
            endDuration = parcel.readLong()
            uri = parcel.readParcelable(null)
        }

        constructor(parent: Parcelable?) : super(parent)


        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeLong(startDuration)
            out.writeLong(endDuration)
            out.writeParcelable(uri, flags)
            out.writeLong(duration)
        }

    }

    interface PositionChangeListener {
        fun onLeftPositionChanged() {}
        fun onRightPositionChanged() {}
        fun onSeekPositionChanged() {}
        fun onLeftHandleReleased() {}
        fun onRightHandleReleased() {}
        fun onSeekHandleReleased() {}
    }

    companion object {
        private const val UPDATE_RANGE = 1
    }

}