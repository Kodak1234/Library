package com.ume.bottomsheet

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.UUID
import kotlin.math.abs

class BottomSheetManager private constructor(
    private val sheet: ViewGroup,
    private val manager: FragmentManager,
    private val state: Bundle?,
) : BottomSheetBehavior.BottomSheetCallback(), IBottomSheetManager {

    private var scrollHelper: ScrollActivatedElevation? = null
    private var op: Op? = null

    private val behavior = BottomSheetBehavior.from(sheet) as SheetBehavior

    init {

        behavior.addBottomSheetCallback(this)
        closeBottomSheet()
        behavior.isFitToContents = true
        behavior.skipCollapsed = true
        manager.registerFragmentLifecycleCallbacks(FragmentWatcher(), false)
    }

    override fun closeBottomSheet() {
        behavior.isHideable = true
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun showBottomSheet(fragment: Fragment, config: SheetConfig) {
        op = Op(fragment, config)
        if (behavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            closeBottomSheet()
        } else {
            onStateChanged(sheet, BottomSheetBehavior.STATE_HIDDEN)
        }
    }

    @SuppressLint("SwitchIntDef", "CommitTransaction")
    override fun onStateChanged(bottomSheet: View, newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_HIDDEN -> {
                val frag = findFragment()
                val detachedFragment =
                    manager.findFragmentByTag(state?.getString(DETACHED_FRAGMENT, ""))

                var transaction: FragmentTransaction? = null

                frag?.let {
                    transaction = manager.beginTransaction()
                    val eventDelegate = frag as? IBottomSheetEventDelegate

                    //if the new fragment is an overlay, don't kill current fragment,
                    //just detach it from the UI
                    if (op?.config?.overlay == true) {
                        state?.putString(DETACHED_FRAGMENT, it.tag)
                        transaction!!.detach(it)
                        eventDelegate?.onBottomSheetHidden()
                    } else {
                        state?.remove(DETACHED_FRAGMENT)
                        transaction!!.remove(it)
                        eventDelegate?.onBottomSheetDismissed()
                    }
                }
                op?.let { op ->
                    this.op = null
                    if (op.frag.arguments == null) {
                        op.frag.arguments = bundleOf(CONFIG to op.config)
                    } else {
                        op.frag.requireArguments().putParcelable(CONFIG, op.config)
                    }

                    transaction = transaction ?: manager.beginTransaction()
                    transaction!!.add(
                        sheet.id,
                        op.frag,
                        op.config.tag ?: UUID.randomUUID().toString()
                    )
                    //if a new fragment is shown while there is a detached fragment,
                    //destroy the detached fragment
                    if (detachedFragment != null) {
                        transaction!!.remove(detachedFragment)
                    }
                }


                transaction?.commit()
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
        manager.findFragmentById(sheet.id)

    private inner class BackPressHandler : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val delegate = findFragment() as? IBottomSheetEventDelegate
            if (delegate == null && behavior.isHideable || delegate?.onBackPress() == false) {
                closeBottomSheet()
            }
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

                val sheet = frag as? IBottomSheetScrollProvider
                scrollHelper = ScrollActivatedElevation(
                    config.cornerRadius,
                    config.elevation, frag.requireView(),
                    sheet?.liftOnScroll, sheet?.scrollingView
                )

                behavior.opacity = config.opacity
                behavior.dimColor = config.dimColor
                behavior.isHideable = config.cancelable
                behavior.allowClickBehind = config.allowClickBehind
                behavior.setPeekHeight(config.peekHeight, true)

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

            val currentFragment = findFragment()
            if (f.id == sheet.id && f.isRemoving) {
                if (currentFragment == null || currentFragment.isDetached) {
                    closeBottomSheet()
                }
                //current fragment is not null means it is a detached fragment
                if (currentFragment != null) {
                    manager.beginTransaction().attach(currentFragment)
                        .commit()
                }
            }
        }

    }

    private class Op(
        val frag: Fragment,
        val config: SheetConfig
    )

    class Builder(private val sheet: ViewGroup) {
        private var fragmentManager: FragmentManager? = null
        private var state: Bundle? = null
        private var owner: LifecycleOwner? = null

        fun setFragmentManager(manager: FragmentManager): Builder {
            fragmentManager = manager
            return this
        }

        fun setState(state: Bundle?): Builder {
            this.state = state
            return this
        }

        fun setOwner(owner: LifecycleOwner): Builder {
            this.owner = owner
            return this
        }

        fun build(): BottomSheetManager {
            when (val host = owner) {
                is Fragment -> {
                    fragmentManager = fragmentManager ?: host.childFragmentManager
                }

                is AppCompatActivity -> {
                    fragmentManager = fragmentManager ?: host.supportFragmentManager
                }
            }

            checkNotNull(fragmentManager) { "setFragmentManager must be called" }
            checkNotNull(owner) { "setOwner must be called" }
            check(store[owner] == null) { "Host already has a manager attached" }

            val manager = BottomSheetManager(sheet, fragmentManager!!, state)
            owner!!.lifecycle.addObserver(BottomSheetManager)
            store[owner!!] = manager

            return manager
        }
    }

    companion object : DefaultLifecycleObserver {

        private const val CONFIG = "com.ume.bottomsheet.config"
        private const val DETACHED_FRAGMENT = "com.ume.bottomsheet.detached-fragment"
        private val store by lazy { mutableMapOf<LifecycleOwner, BottomSheetManager>() }

        fun find(host: LifecycleOwner): BottomSheetManager? {
            return store[host]
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            owner.lifecycle.removeObserver(this)
            store.remove(owner)
        }
    }
}