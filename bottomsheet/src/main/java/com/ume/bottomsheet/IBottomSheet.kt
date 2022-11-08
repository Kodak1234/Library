package com.ume.bottomsheet

import android.view.View

interface IBottomSheet {

    val scrollingView: View?
        get() = null

    val liftOnScroll: View?
        get() = null

    /*This bottom sheet is hidden. The fragment should be removed
    * in this method*/
    fun onBottomSheetClosed()
}