package com.ume.bottomsheet

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.abs

class BottomSheetManager private constructor(
    private val sheet: ViewGroup,
    private val host: LifecycleOwner,
) : BottomSheetBehavior.BottomSheetCallback(), IBottomSheetManager {

    private var scrollHelper: ScrollActivatedElevation? = null
    private var op: Op? = null

    private val behavior = BottomSheetBehavior.from(sheet) as SheetBehavior
    private val mn = when (host) {
        is Fragment -> host.childFragmentManager
        is AppCompatActivity -> host.supportFragmentManager
        else -> throw IllegalArgumentException("Host must be a fragment or an activity")
    }

    init {

        behavior.addBottomSheetCallback(this)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.isHideable = true
        behavior.isFitToContents = true

        mn.registerFragmentLifecycleCallbacks(FragmentWatcher(), false)
    }

    override fun closeBottomSheet() {
        behavior.isHideable = true
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun showBottomSheet(fragment: Fragment, config: SheetConfig) {
        op = Op(fragment, config)
        if (behavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            closeBottomSheet()
        } else {
            onStateChanged(sheet, BottomSheetBehavior.STATE_COLLAPSED)
        }
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_HIDDEN -> {
                val frag = findFragment()
                if (frag is IBottomSheet)
                    frag.onBottomSheetClosed()

                if (frag != null) {
                    mn.beginTransaction().remove(frag).commit()
                } else {
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                val op = this.op
                if (op != null) {
                    this.op = null

                    if (op.frag.arguments == null) {
                        op.frag.arguments = bundleOf(CONFIG to op.config)
                    } else {
                        op.frag.requireArguments().putParcelable(CONFIG, op.config)
                    }

                    mn.beginTransaction()
                        .replace(sheet.id, op.frag, op.config.tag)
                        .commit()
                }
            }
        }

    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {

        if (getConfig()?.animateRadius == true) {
            val parent = bottomSheet.parent as View
            var offset = 1f - abs(slideOffset)
            //no radius when bottom sheet is fully expanded to match parent height
            if (bottomSheet.top == 0) {
                offset = 0f
            }
            scrollHelper?.setInterpolation(offset)
        }
    }


    private fun getConfig(): SheetConfig? {
        val args = findFragment()?.arguments
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            args?.getParcelable(CONFIG, SheetConfig::class.java)
        } else {
            args?.getParcelable(CONFIG)
        }
    }

    private fun findFragment(): Fragment? =
        mn.findFragmentById(sheet.id)

    private inner class BackPressHandler : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private inner class FragmentWatcher : FragmentLifecycleCallbacks() {

        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
            super.onFragmentStarted(fm, f)
            if (f.id == sheet.id && behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        override fun onFragmentViewCreated(
            fm: FragmentManager,
            frag: Fragment,
            child: View,
            savedInstanceState: Bundle?
        ) {
            if (frag.id == sheet.id) {
                val config = checkNotNull(getConfig())

                val sheet = frag as? IBottomSheet
                scrollHelper = ScrollActivatedElevation(
                    config.cornerRadius,
                    config.elevation, frag.requireView(),
                    sheet?.liftOnScroll, sheet?.scrollingView
                )

                behavior.opacity = config.opacity
                behavior.dimColor = config.dimColor
                behavior.isHideable = config.cancelable
                behavior.allowClickBehind = config.allowClickBehind

                BackgroundUtil.setBackground(
                    frag.requireView(),
                    config.cornerRadius,
                    BackgroundUtil.getColor(frag.requireView())
                )
                checkNotNull(BackgroundUtil.getBackground(frag.requireView()))
                    .elevation = config.elevation

                frag.requireActivity().onBackPressedDispatcher.addCallback(
                    frag.viewLifecycleOwner,
                    BackPressHandler()
                )
            }
        }

        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            super.onFragmentViewDestroyed(fm, f)

            if (f.id == sheet.id && f.isRemoving) {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private class Op(
        val frag: Fragment,
        val config: SheetConfig
    )

    companion object : DefaultLifecycleObserver {

        private const val CONFIG = "com.ume.bottomsheet.config"
        private val store by lazy { mutableMapOf<LifecycleOwner, BottomSheetManager>() }

        fun attach(
            host: LifecycleOwner,
            sheet: ViewGroup
        ): IBottomSheetManager {
            check(store[host] == null) { "Host already has a manager attached" }
            val mn = BottomSheetManager(sheet, host)
            host.lifecycle.addObserver(this)
            store[host] = mn
            return mn
        }

        fun find(host: LifecycleOwner): IBottomSheetManager? {
            return store[host]
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            owner.lifecycle.removeObserver(this)
            store.remove(owner)
        }
    }
}