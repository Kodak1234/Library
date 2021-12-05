package com.ume.libraryapplication

import android.os.Bundle
import android.view.View
import com.ume.bottomsheet.BottomSheetFragment

class BottomSheetExample : BottomSheetFragment(R.layout.fragment_bottom_sheet) {

    override val cancelable: Boolean = false
    override val scrollId: Int = R.id.scroller
    override val liftId: Int = R.id.bar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button = view.findViewById<View>(R.id.closeButton)
        button.setOnClickListener { hide() }
    }

    override fun onBottomSheetHidden() {
        parentFragmentManager.beginTransaction()
            .remove(this)
            .commit()
    }
}