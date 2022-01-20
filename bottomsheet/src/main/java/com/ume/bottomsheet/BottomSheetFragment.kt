package com.ume.bottomsheet

import androidx.fragment.app.Fragment

abstract class BottomSheetFragment : Fragment,
    IBottomSheet {

    constructor(layout: Int) : super(layout)

    constructor() : super()

    override val cornerRadius: Float
        get() = resources.getDimension(R.dimen.defaultRadius)
    override val liftElevation: Float
        get() = resources.getDimension(R.dimen.defaultElevation)
    override val elevation: Float
        get() = resources.getDimension(R.dimen.defaultElevation)

    fun getBottomSheetManager(): IBottomSheetManager =
        BottomSheetManager.find(this)!!

    fun hide() {
        getBottomSheetManager().hideBottomSheet()
    }
}