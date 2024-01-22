package com.ume.bottomsheet

interface IBottomSheetEventDelegate {

    //Fragment is in a detached state
    fun onBottomSheetDismissed() {}

    //fragment is destroyed
    fun onBottomSheetHidden() {}

    fun onBackPress(): Boolean = false
}