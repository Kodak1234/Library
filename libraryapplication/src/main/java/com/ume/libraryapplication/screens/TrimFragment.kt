package com.ume.libraryapplication.screens

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.ume.libraryapplication.R
import com.ume.libraryapplication.screens.glide.GlideApp
import com.ume.trimview.ui.TrimView

class TrimFragment : Fragment(R.layout.fragment_trim), TrimView.PositionChangeListener {

    private lateinit var launcher: ActivityResultLauncher<Array<String>>
    private var trim: TrimView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launcher = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            if (it != null)
                trim?.setUri(it, getDuration(it))
        }
    }

    private fun getDuration(uri: Uri): Long {
        val meta = MediaMetadataRetriever()
        meta.setDataSource(requireContext(), uri)
        val d = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
        meta.release()
        return d

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trim = view.findViewById(R.id.trim)
        trim!!.positionChangeListener = this
        launcher.launch(arrayOf("video/*"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        trim = null
    }

    override fun onLeftPositionChanged(pos: Long, leftHandle: View) {
        Log.d(TAG, "onLeftPositionChanged() called with: pos = $pos")
    }

    override fun onRightPositionChanged(pos: Long, rightHandle: View) {
        Log.d(TAG, "onRightPositionChanged() called with: pos = $pos")
    }

    override fun onSeekPositionChanged(pos: Long, seekHandle: View) {
        Log.d(TAG, "onSeekPositionChanged() called with: pos = $pos")
    }

    companion object {
        private const val TAG = "TrimFragment"
    }
}