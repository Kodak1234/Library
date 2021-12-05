package com.ume.bottomsheet

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentResultListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import kotlin.math.max

class BottomSheetWatcher private constructor(
    private val scrim: View?,
    private val sheet: ViewGroup
) : BottomSheetBehavior.BottomSheetCallback(), ViewGroup.OnHierarchyChangeListener,
    FragmentResultListener {

    private val behavior = (sheet.layoutParams as LayoutParams).behavior
            as BottomSheetBehavior
    private val context: FragmentActivity = sheet.context as FragmentActivity
    private val defaultRadius = context.resources.getDimension(R.dimen.defaultRadius)
    private val defaultElevation = context.resources.getDimension(R.dimen.defaultElevation)

    init {
        sheet.setOnHierarchyChangeListener(this)
        scrim?.setOnClickListener {}
        behavior.addBottomSheetCallback(this)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.isHideable = true
        behavior.isDraggable = false

        setBackground(sheet, 0f)
        context.supportFragmentManager.setFragmentResultListener(
            DISMISS,
            context, this
        )
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
            val frag = findFragment()
            if (frag is IBottomSheet) frag.dismissBottomSheet()
        }
    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        val liftHelper = findHelper()
        if (liftHelper?.enable == true)
            liftHelper.setInterpolation(1 - max(slideOffset, 0f))
    }

    override fun onChildViewAdded(parent: View, child: View) {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = true
        scrim?.isVisible = true

        val frag = findFragment()
        if (frag is ILiftable) {
            val elevation = frag.getFloatValue(ELEVATION, defaultElevation)
            val radius = frag.getFloatValue(RADIUS, defaultRadius)

            getBackground(sheet)?.apply {
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                    .setTopRightCorner(CornerFamily.ROUNDED, radius)
                    .build()
            }

            sheet.setTag(
                sheet.id, LiftHelper(
                    radius, elevation, sheet,
                    child.findViewById(frag.liftId),
                    child.findViewById(frag.scrollId)
                )
            )
        }
    }

    override fun onChildViewRemoved(parent: View, child: View) {
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        scrim?.isVisible = false
        behavior.isDraggable = false
        sheet.setTag(sheet.id, null)
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun findHelper(): LiftHelper? = sheet.getTag(sheet.id) as LiftHelper?

    private fun findFragment(): Fragment? =
        context.supportFragmentManager
            .findFragmentById(sheet.id)

    class LiftHelper(
        radius: Float,
        private val elevation: Float,
        private val sheet: ViewGroup,
        private val lift: View?,
        private val scroll: View?
    ) : NestedScrollView.OnScrollChangeListener, RecyclerView.OnScrollListener() {

        var enable = true
            set(value) {
                if (!value && value != field)
                    setInterpolation(1f)
                field = value
            }


        init {
            setBackground(lift, radius)
            when (scroll) {
                is NestedScrollView -> scroll.setOnScrollChangeListener(this)
                is RecyclerView -> scroll.addOnScrollListener(this)
            }
        }

        override fun onScrollChange(
            v: NestedScrollView?,
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
            getBackground(lift)?.interpolation = value
            getBackground(sheet)?.interpolation = value
        }

        private fun onScrollChange(scrollY: Int) {
            if (scrollY > 0 && enable) {
                ViewCompat.setElevation(lift!!, elevation)
            } else if (scrollY == 0 && enable) {
                ViewCompat.setElevation(lift!!, 0f)
            }
        }
    }

    companion object {
        const val DISMISS = "com.ume.bottomsheet.dismiss-key"
        const val RADIUS = "com.ume.bottomsheet.radius-key"
        const val ELEVATION = "com.ume.notes.elevation-key"
        fun watch(scrim: View, sheet: ViewGroup) = BottomSheetWatcher(scrim, sheet)

        private fun setBackground(view: View?, radius: Float) {
            if (view != null) {
                ViewCompat.setBackground(view, MaterialShapeDrawable().apply {
                    fillColor = ColorStateList.valueOf(
                        if (view.background is ColorDrawable)
                            (view.background as ColorDrawable).color
                        else {
                            MaterialColors.getColor(
                                view,
                                R.attr.colorSurface,
                                Color.WHITE
                            )
                        }
                    )
                    shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                        .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                        .setTopRightCorner(CornerFamily.ROUNDED, radius)
                        .build()
                })
            }
        }

        private fun getBackground(view: View?): MaterialShapeDrawable? {
            return view?.background as? MaterialShapeDrawable?
        }

        private fun Fragment.getFloatValue(key: String, def: Float): Float {
            return arguments?.getFloat(key, def) ?: def
        }
    }
}