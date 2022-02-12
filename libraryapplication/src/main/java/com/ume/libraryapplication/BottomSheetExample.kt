package com.ume.libraryapplication

import android.graphics.Color
import com.ume.bottomsheet.BottomSheetFragment

class BottomSheetExample : BottomSheetFragment(R.layout.fragment_bottom_sheet) {

    override val cancelable: Boolean = false
    override val liftId: Int = R.id.bar

    override fun onBottomSheetHidden() {
        parentFragmentManager.beginTransaction()
            .remove(this)
            .commit()
    }
}