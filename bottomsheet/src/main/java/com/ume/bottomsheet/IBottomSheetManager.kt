package com.ume.bottomsheet

import androidx.fragment.app.Fragment

interface IBottomSheetManager {
    fun closeBottomSheet()
    fun showBottomSheet(fragment: Fragment, config: SheetConfig)
}