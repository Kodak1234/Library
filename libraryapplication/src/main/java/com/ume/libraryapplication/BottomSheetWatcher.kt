package com.ume.libraryapplication

import android.view.View
import com.ume.guidedtour.ISceneWatcher
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED

class BottomSheetWatcher(private val behavior: BottomSheetBehavior<*>) : ISceneWatcher {

    override fun watchScene(notify: () -> Unit) {
        if (behavior.state == STATE_EXPANDED)
            notify()
        else {
            behavior.addBottomSheetCallback(object : BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == STATE_EXPANDED) {
                        behavior.removeBottomSheetCallback(this)
                        notify()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }
            })
        }
    }
}