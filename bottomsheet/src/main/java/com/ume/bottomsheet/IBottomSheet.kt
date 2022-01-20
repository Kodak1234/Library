package com.ume.bottomsheet

import android.view.View

interface IBottomSheet {

    val scrollId: Int
        get() = View.NO_ID
    val liftId: Int
        get() = View.NO_ID
    val cancelable: Boolean
        get() = false
    val elevation: Float
        get() = 0f
    val backgroundColor: Int?
        get() = null
    val cornerRadius: Float
        get() = 0f
    val liftElevation: Float
        get() = 0f

    /*This bottom sheet is hidden. The fragment should be removed
    * in this method*/
    fun onBottomSheetHidden()
}