package ume.recyclerview.draghelper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

abstract class RecyclerViewDragHelper(
    private val dummy: ImageView,
    dragDirs: Int
) : SimpleItemTouchCallback(dragDirs, 0) {
    private val loc = IntArray(2) { 0 }
    private var bitmap: Bitmap? = null
    private val rect = Rect()
    abstract val dst: View
    var state = State.STATE_IDLE
        private set

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: ViewHolder, dX: Float,
        dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val view = viewHolder.itemView

        if (isCurrentlyActive) {
            if (state == State.STATE_IDLE) {
                state = State.STATE_OUT_BOUND
                bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap!!)
                view.draw(canvas)
                dummy.setImageBitmap(bitmap)
                dummy.isInvisible = true
                onDragBegin(dummy, viewHolder)
            }

            if (state != State.STATE_IDLE) {
                calculateDisplacement(view)
                onDrag(dummy, viewHolder, loc[0], loc[1])
                if (intersects(dst)) {
                    if (state == State.STATE_OUT_BOUND) {
                        state = State.STATE_IN_BOUND
                        onEnterBoundary(dummy, dst, viewHolder)
                    }
                } else if (state == State.STATE_IN_BOUND) {
                    state = State.STATE_OUT_BOUND
                    onExitBoundary(dummy, dst, viewHolder)
                }

                //only make dummy visible when it
                //is aligned with the view being dragged
                if (!dummy.isVisible) {
                    calculateDisplacement(view)
                    if (loc[0] == 0 && loc[1] == 0 && (dX != 0f || dY != 0f))
                        dummy.isVisible = true
                }
            }
        } else {
            if (state == State.STATE_IN_BOUND) {
                val settle = onReleasedInsideBoundary(dummy, dst, viewHolder)
                state = if (settle) State.STATE_SETTLE else State.STATE_DOCK
            } else if (state == State.STATE_OUT_BOUND) {
                state = State.STATE_SETTLE
                onReleaseOutSideBoundary(dummy, dst, viewHolder)
            }
            if (state == State.STATE_SETTLE) {
                calculateDisplacement(view)
                onSettle(dummy, viewHolder, loc[0], loc[1])
            }
        }

    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        state = State.STATE_IDLE
        bitmap?.recycle()
        dummy.isInvisible = true
    }


    open fun onDrag(dummy: ImageView, viewHolder: ViewHolder, dx: Int, dy: Int) {
        ViewCompat.offsetLeftAndRight(dummy, dx)
        ViewCompat.offsetTopAndBottom(dummy, dy)
    }

    open fun onSettle(dummy: ImageView, viewHolder: ViewHolder, dx: Int, dy: Int) {
        ViewCompat.offsetLeftAndRight(dummy, dx)
        ViewCompat.offsetTopAndBottom(dummy, dy)
    }

    open fun onDragBegin(dummy: ImageView, viewHolder: ViewHolder) {

    }

    open fun onEnterBoundary(dummy: ImageView, dst: View, viewHolder: ViewHolder) {

    }

    open fun onExitBoundary(dummy: ImageView, dst: View, viewHolder: ViewHolder) {

    }

    open fun onReleasedInsideBoundary(dummy: ImageView, dst: View, viewHolder: ViewHolder)
            : Boolean = true

    open fun onReleaseOutSideBoundary(dummy: ImageView, dst: View, viewHolder: ViewHolder) {

    }

    open fun intersects(view: View): Boolean {
        view.getLocationOnScreen(loc)
        val left = loc[0]
        val right = left + view.width
        val top = loc[1]
        val bottom = top + view.height

        dummy.getLocationOnScreen(loc)
        rect.set(loc[0], loc[1], loc[0] + dummy.width, loc[1] + dummy.height)
        return rect.intersects(left, top, right, bottom)
    }

    private fun calculateDisplacement(view: View) {
        view.getLocationOnScreen(loc)
        val vx = loc[0]
        val vy = loc[1]
        dummy.getLocationOnScreen(loc)

        loc[0] = vx - loc[0]
        loc[1] = vy - loc[1]
    }

    enum class State {
        STATE_IDLE,
        STATE_SETTLE,
        STATE_DOCK,
        STATE_IN_BOUND,
        STATE_OUT_BOUND
    }
}