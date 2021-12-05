package com.ume.bottomsheet

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import kotlin.math.max

class BottomSheetManager private constructor(
    private val scrim: View?,
    private val sheet: ViewGroup
) : BottomSheetBehavior.BottomSheetCallback() {

    private val behavior = (sheet.layoutParams as LayoutParams).behavior
            as BottomSheetBehavior
    private val context: FragmentActivity = sheet.context as FragmentActivity

    init {
        scrim?.setOnClickListener {}
        behavior.addBottomSheetCallback(this)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.isHideable = true
        behavior.isDraggable = false

        setBackground(sheet, 0f)
        context.supportFragmentManager.registerFragmentLifecycleCallbacks(
            FragmentWatcher(), false
        )
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (newState == STATE_HIDDEN) {
            val frag = findFragment()
            if (frag is IBottomSheet)
                frag.close()
        }
    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        val liftHelper = findHelper()
        if (liftHelper?.enable == true)
            liftHelper.setInterpolation(1 - max(slideOffset, 0f))
    }

    private fun findHelper(): LiftHelper? =
        sheet.getTag(R.id.lift_helper) as LiftHelper?

    private fun findFragment(): Fragment? =
        context.supportFragmentManager
            .findFragmentById(sheet.id)

    inner class FragmentWatcher : FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            frag: Fragment,
            child: View,
            savedInstanceState: Bundle?
        ) {
            if (frag.id == sheet.id) {
                if (frag is IBottomSheet) {
                    val elevation = frag.getLiftElevation()
                    val radius = frag.getCornerRadius()
                    getBackground(sheet)?.apply {
                        shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                            .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                            .setTopRightCorner(CornerFamily.ROUNDED, radius)
                            .build()
                    }

                    sheet.setTag(
                        R.id.lift_helper, LiftHelper(
                            radius, elevation, sheet,
                            child.findViewById(frag.liftId),
                            child.findViewById(frag.scrollId)
                        )
                    )
                }
                frag.lifecycleScope.launchWhenStarted {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.isDraggable = true
                    scrim?.isVisible = true
                }
            }
        }

        override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
            super.onFragmentStopped(fm, f)
            if (f.id == sheet.id && f.isRemoving) {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                scrim?.isVisible = false
                behavior.isDraggable = false
                sheet.setTag(sheet.id, null)
            }
        }
    }

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

        fun attach(scrim: View?, sheet: ViewGroup): BottomSheetManager {
            val watcher = BottomSheetManager(scrim, sheet)
            sheet.setTag(R.id.bottom_sheet_manager, watcher)
            return watcher
        }

        fun find(frag: Fragment): BottomSheetManager? {
            val sheet = frag.requireActivity().findViewById<View>(frag.id)
            return if (sheet != null)
                sheet.getTag(R.id.bottom_sheet_manager) as? BottomSheetManager?
            else null
        }

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
    }
}