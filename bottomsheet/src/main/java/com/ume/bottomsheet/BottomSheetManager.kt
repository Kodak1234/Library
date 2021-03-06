package com.ume.bottomsheet

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import kotlin.math.max

class BottomSheetManager private constructor(
    private val scrim: View?,
    private val sheet: ViewGroup,
    private val behavior: BottomSheetBehavior<*>,
    private val context: FragmentActivity
) : BottomSheetBehavior.BottomSheetCallback(), IBottomSheetManager {

    private var pendingState: PendingState? = null

    init {
        //capture clicks inside sheet bounds
        sheet.isSoundEffectsEnabled = false
        sheet.setOnClickListener { }
        scrim?.setOnClickListener {
            if (behavior.isHideable)
                behavior.state = STATE_HIDDEN
        }
        behavior.addBottomSheetCallback(this)
        behavior.state = STATE_COLLAPSED
        behavior.isHideable = true
        behavior.isDraggable = false

        context.supportFragmentManager.registerFragmentLifecycleCallbacks(
            FragmentWatcher(), false
        )
    }

    override fun hideBottomSheet() {
        behavior.isHideable = true
        behavior.state = STATE_HIDDEN
    }

    override fun showBottomSheet(fragment: Fragment, tag: String?) {
        when (behavior.state) {
            STATE_EXPANDED -> {
                if (tag == null || findFragment()?.tag != tag) {
                    pendingState = PendingState(fragment, tag)
                    hideBottomSheet()
                }
            }
            else -> showInternal(fragment, tag)
        }
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        when (newState) {
            STATE_HIDDEN -> {
                val frag = findFragment()
                if (frag is IBottomSheet)
                    frag.onBottomSheetHidden()
                else if (frag == null)
                    behavior.state = STATE_COLLAPSED
            }
            STATE_COLLAPSED -> {
                if (pendingState != null) {
                    showInternal(pendingState!!.frag, pendingState!!.tag)
                    pendingState = null
                }
            }
        }

    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        val liftHelper = findHelper()
        if (liftHelper?.enable == true)
            liftHelper.setInterpolation(1 - max(slideOffset, 0f))
    }

    private fun showInternal(frag: Fragment, tag: String?) {
        val mn = context.supportFragmentManager
        mn.beginTransaction()
            .replace(sheet.id, frag, tag)
            .commit()
    }

    private fun findHelper(): LiftHelper? =
        sheet.getTag(R.id.lift_helper) as LiftHelper?

    private fun findFragment(): Fragment? =
        context.supportFragmentManager
            .findFragmentById(sheet.id)

    inner class BackPressHandler : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (behavior.isHideable)
                hideBottomSheet()
        }
    }

    inner class FragmentWatcher : FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            frag: Fragment,
            child: View,
            savedInstanceState: Bundle?
        ) {
            if (frag.id == sheet.id) {
                if (frag is IBottomSheet) {
                    val elevation = frag.liftElevation
                    val radius = frag.cornerRadius
                    setBackground(sheet, radius, frag.backgroundColor)
                    ViewCompat.setElevation(sheet, frag.elevation)
                    sheet.setTag(
                        R.id.lift_helper, LiftHelper(
                            radius, elevation, sheet,
                            child.findViewById(frag.liftId),
                            child.findViewById(frag.scrollId)
                        )
                    )
                }
                context.onBackPressedDispatcher.addCallback(
                    frag.viewLifecycleOwner,
                    BackPressHandler()
                )
                frag.lifecycleScope.launchWhenStarted {
                    behavior.state = STATE_EXPANDED
                    behavior.isDraggable = true
                    behavior.isHideable = frag !is IBottomSheet || frag.cancelable
                    scrim?.isVisible = true
                }
            }
        }

        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            super.onFragmentViewDestroyed(fm, f)
            //if current attached fragment does not equal then don't hide
            //bottom sheet.
            val active = findFragment()
            if (f.id == sheet.id && f.isRemoving &&
                (active == f || active == null)
            ) {
                behavior.state = STATE_COLLAPSED
                scrim?.isVisible = false
                behavior.isDraggable = false
                behavior.isHideable = true
                sheet.setTag(R.id.lift_helper, null)
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

    class PendingState(val frag: Fragment, val tag: String?)

    companion object {

        fun attach(
            context: FragmentActivity,
            scrim: View?,
            container: ViewGroup,
            behavior: BottomSheetBehavior<*>
        ): IBottomSheetManager {
            val content = context.findViewById<View>(android.R.id.content)
            if (content.getTag(R.id.bottom_sheet_manager) != null)
                throw UnsupportedOperationException("Bottom sheet manager is already attached")
            val mn = BottomSheetManager(scrim, container, behavior, context)
            content.setTag(R.id.bottom_sheet_manager, mn)
            return mn
        }

        fun find(context: FragmentActivity): IBottomSheetManager? {
            return context.findViewById<View>(android.R.id.content)
                .getTag(R.id.bottom_sheet_manager) as IBottomSheetManager?
        }

        fun find(frag: Fragment): IBottomSheetManager? = find(frag.requireActivity())

        private fun setBackground(
            view: View?, radius: Float,
            color: Int? = null
        ) {
            if (view != null) {
                ViewCompat.setBackground(view, MaterialShapeDrawable().apply {
                    fillColor = ColorStateList.valueOf(
                        when {
                            color != null -> color
                            view.background is ColorDrawable -> (view.background as ColorDrawable).color
                            else -> MaterialColors.getColor(
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