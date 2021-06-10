package com.ume.selection

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks

fun Fragment.manageState(selection: SelectionHelper) {
    val container = this
    requireFragmentManager().registerFragmentLifecycleCallbacks(
        object : FragmentLifecycleCallbacks() {
            override fun onFragmentSaveInstanceState(
                fm: FragmentManager,
                f: Fragment,
                outState: Bundle
            ) {
                super.onFragmentSaveInstanceState(fm, f, outState)
                if (f == container)
                    selection.save(outState)
            }

            override fun onFragmentCreated(
                fm: FragmentManager,
                f: Fragment,
                savedInstanceState: Bundle?
            ) {
                super.onFragmentCreated(fm, f, savedInstanceState)
                if (f == container)
                    selection.restore(savedInstanceState)
            }
        }, false
    )


}