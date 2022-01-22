package com.ume.bottomsheet

import androidx.fragment.app.Fragment

interface IBottomSheetManager {
    fun hideBottomSheet()
    fun showBottomSheet(fragment: Fragment)
}