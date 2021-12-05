package com.ume.bottomsheet

import android.view.View
import androidx.fragment.app.Fragment

abstract class BottomSheetFragment(layoutId: Int) : Fragment(layoutId),
    IBottomSheet {

    override val scrollId: Int = View.NO_ID
    override val liftId: Int = View.NO_ID

    override fun getCornerRadius(): Float =
        resources.getDimension(R.dimen.defaultRadius)

    override fun getLiftElevation(): Float =
        resources.getDimension(R.dimen.defaultElevation)

    fun getBottomSheetManager(): BottomSheetManager =
        BottomSheetManager.find(this)!!
}