package com.ume.bottomsheet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult

abstract class BottomSheetFragment(layoutId: Int) : Fragment(layoutId),
    ILiftable, IBottomSheet {

    override val scrollId: Int = View.NO_ID
    override val liftId: Int = View.NO_ID

    fun setLiftElevation(elevation: Float) {
        checkNotStarted()
        arguments?.putFloat(BottomSheetWatcher.ELEVATION, elevation)
    }

    fun setCornerRadius(radius: Float) {
        checkNotStarted()
        arguments?.putFloat(BottomSheetWatcher.RADIUS, radius)
    }

    override fun dismissBottomSheet() {
        setFragmentResult(BottomSheetWatcher.DISMISS, Bundle())
    }

    private fun checkNotStarted() {
        if (view != null)
            throw IllegalStateException("Method must be called before onCreateView")
    }
}