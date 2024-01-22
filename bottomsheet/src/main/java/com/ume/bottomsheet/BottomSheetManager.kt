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
import androidx.savedstate.SavedStateRegistry
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.UUID
import kotlin.math.abs

class BottomSheetManager private constructor(
    private val sheet: ViewGroup,
    host: LifecycleOwner,
) : BottomSheetBehavior.BottomSheetCallback(), IBottomSheetManager,
    SavedStateRegistry.SavedStateProvider {

    private var scrollHelper: ScrollActivatedElevation? = null
    private var op: Op? = null
    private var detachedFragmentTag: String = ""

    private val behavior = BottomSheetBehavior.from(sheet) as SheetBehavior
    private val mn = when (host) {
        is Fragment -> host.childFragmentManager
        is AppCompatActivity -> host.supportFragmentManager
        else -> throw IllegalArgumentException("Host must be a fragment or an activity")
    }

    private val stateRegistry = when (host) {
        is Fragment -> host.savedStateRegistry
        is AppCompatActivity -> host.savedStateRegistry
        else -> throw IllegalArgumentException("Host must be a fragment or an activity")
    }

    init {

        detachedFragmentTag = stateRegistry.consumeRestoredStateForKey(STATE)
            ?.getString(DETACHED_FRAGMENT) ?: ""
        behavior.addBottomSheetCallback(this)
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        behavior.isHideable = true
        behavior.isFitToContents = true
        behavior.skipCollapsed = true
        stateRegistry.registerSavedStateProvider(STATE, this)
        mn.registerFragmentLifecycleCallbacks(FragmentWatcher(), false)
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
                val detachedFragment = mn.findFragmentByTag(detachedFragmentTag)

                var transaction: FragmentTransaction? = null

                frag?.let {
                    transaction = mn.beginTransaction()
                    val eventDelegate = frag as? IBottomSheetEventDelegate

                    //if the new fragment is an overlay, don't kill current fragment,
                    //just detach it from the UI
                    if (op?.config?.overlay == true) {
                        detachedFragmentTag = it.tag!!
                        transaction!!.detach(it)
                        eventDelegate?.onBottomSheetHidden()
                    } else {
                        detachedFragmentTag = ""
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

                    transaction = transaction ?: mn.beginTransaction()
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
        mn.findFragmentById(sheet.id)

    override fun saveState(): Bundle = bundleOf(DETACHED_FRAGMENT to detachedFragmentTag)

    private inner class BackPressHandler : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val delegate = findFragment() as? IBottomSheetEventDelegate
            if (delegate?.onBackPress() == false) {
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
                    mn.beginTransaction().attach(currentFragment)
                        .commit()
                }
            }
        }

    }

    private class Op(
        val frag: Fragment,
        val config: SheetConfig
    )

    companion object : DefaultLifecycleObserver {

        private const val STATE = "com.ume.bottomsheet.manager-state"
        private const val CONFIG = "com.ume.bottomsheet.config"
        private const val DETACHED_FRAGMENT = "com.ume.bottomsheet.detached-fragment"
        private val store by lazy { mutableMapOf<LifecycleOwner, BottomSheetManager>() }

        fun attach(
            host: LifecycleOwner,
            sheet: ViewGroup
        ): BottomSheetManager {
            check(store[host] == null) { "Host already has a manager attached" }
            val mn = BottomSheetManager(sheet, host)
            host.lifecycle.addObserver(this)
            store[host] = mn
            return mn
        }

        fun find(host: LifecycleOwner): BottomSheetManager? {
            return store[host]
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            find(owner)?.stateRegistry?.unregisterSavedStateProvider(STATE)
            owner.lifecycle.removeObserver(this)
            store.remove(owner)
        }
    }
}