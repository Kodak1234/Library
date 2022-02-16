package com.ume.libraryapplication

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.ume.bottomsheet.BottomSheetFragment
import com.ume.picker.data.MediaItem
import com.ume.picker.ui.PickerFragment

class BottomSheetExample : BottomSheetFragment(R.layout.fragment_bottom_sheet),
    PickerFragment.Callback {

    override val cancelable: Boolean = false
    override val liftId: Int = R.id.title
    private var count = 0
    private lateinit var title: TextView



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = view.findViewById(R.id.title)
    }

    override fun onMediaSelected(media: MediaItem): Boolean {
        Log.i(TAG, "onMediaSelected: ")
        if(count == 6)
            return false
        count++
        update()
        return true
    }

    override fun onMediaUnSelected(media: MediaItem) {
        Log.i(TAG, "onMediaUnSelected: ")
        count--
        update()
    }

    override fun onMediaSizeChanged(size: Int) {
        Log.i(TAG, "onMediaSizeChanged: ")
    }

    @SuppressLint("SetTextI18n")
    private fun update() {
        title.text = "$count media${if (count > 1) "s" else ""} selected."
    }

    override fun onBottomSheetHidden() {
        parentFragmentManager.beginTransaction()
            .remove(this)
            .commit()
    }

    companion object {
        private const val TAG = "BottomSheetExample"
    }
}