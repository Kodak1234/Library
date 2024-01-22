package com.ume.libraryapplication

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ume.bottomsheet.IBottomSheetEventDelegate
import com.ume.picker.data.MediaItem
import com.ume.picker.ui.PickerFragment
import com.ume.util.hasPermission

class BottomSheetExample : Fragment(R.layout.fragment_bottom_sheet),
    PickerFragment.Callback,IBottomSheetEventDelegate {

    private var count = 0
    private lateinit var title: TextView


    override fun onStart() {
        super.onStart()
        if (!hasPermission(READ_EXTERNAL_STORAGE))
            requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), 11)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = view.findViewById(R.id.title)
    }

    override fun onMediaSelected(media: MediaItem): Boolean {
        Log.i(TAG, "onMediaSelected: ")
        if (count == 6)
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


    companion object {
        private const val TAG = "BottomSheetExample"
    }
}