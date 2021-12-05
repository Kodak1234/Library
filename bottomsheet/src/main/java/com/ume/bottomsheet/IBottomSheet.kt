package com.ume.bottomsheet

interface IBottomSheet {

    val scrollId: Int
    val liftId: Int
    val cancelable: Boolean

    /*This bottom sheet is hidden. The fragment should be removed
    * in this method*/
    fun onBottomSheetHidden()

    fun getCornerRadius(): Float

    fun getLiftElevation(): Float
}