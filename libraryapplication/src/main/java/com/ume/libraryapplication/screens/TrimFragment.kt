package com.ume.libraryapplication.screens

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.ume.libraryapplication.R
import com.ume.trimview.ui.TrimView

class TrimFragment : Fragment(R.layout.fragment_trim), TrimView.PositionChangeListener,
    Player.Listener {

    private lateinit var launcher: ActivityResultLauncher<Array<String>>
    private lateinit var trim: TrimView
    private lateinit var playButton: AppCompatImageView
    private lateinit var player: Player
    private var uri: Uri? = null
    private val handler = Handler(Looper.getMainLooper())
    private val updateProgress = object : Runnable {
        override fun run() {
            trim.seekTo(player.currentPosition)
            if (player.isPlaying)
                handler.post(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uri = savedInstanceState?.getParcelable("URI")
        player = ExoPlayer.Builder(requireContext()).build()
        player.addListener(this)
        launcher = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            if (it != null) {
                uri = it
                val duration = getDuration(uri!!)
                trim.setUri(uri!!, duration)
                prepareToPlay()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trim = view.findViewById(R.id.trim)
        trim.addPositionListener(this)


        val playerView = view.findViewById<PlayerView>(R.id.playerView)
        playButton = playerView.findViewById(R.id.playButton)
        var clip = savedInstanceState != null
        playButton.setOnClickListener {
            if (player.isPlaying)
                player.pause()
            else {
                if (player.playbackState == Player.STATE_ENDED)
                    player.seekToDefaultPosition()
                if (clip)
                    clipVideo(trim.getStartDuration(), trim.getEndDuration())
                player.play()
                clip = false
            }
        }
        playerView.player = player

        if (uri == null)
            launcher.launch(arrayOf("video/*"))
        else {
            prepareToPlay()
        }

    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.stop()
        player.release()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        playButton.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        if (isPlaying)
            handler.post(updateProgress)

    }

    override fun onLeftHandleReleased() {
        super.onLeftHandleReleased()
        clipVideo(trim.getStartDuration(), trim.getEndDuration())

        Log.d(TAG, "onLeftHandleReleased() called with: duration = ${trim.getStartDuration()}")
    }

    override fun onRightHandleReleased() {
        super.onRightHandleReleased()
        clipVideo(trim.getStartDuration(), trim.getEndDuration())
        Log.d(TAG, "onRightHandleReleased() called with: duration = ${trim.getEndDuration()}")
    }

    override fun onSeekHandleReleased() {
        super.onSeekHandleReleased()
        player.seekTo(trim.getSeekDuration())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("URI", uri)
    }

    private fun clipVideo(start: Long, end: Long) {

        val clip = MediaItem.ClippingConfiguration.Builder()
        if (start > 0)
            clip.setStartPositionMs(start)
        if (end > 0)
            clip.setEndPositionMs(end)

        val item = MediaItem.Builder()
            .setUri(uri!!)
            .setClippingConfiguration(clip.build())
            .build()

        player.clearMediaItems()
        player.addMediaItem(item)
        player.seekToDefaultPosition()

    }

    private fun getDuration(uri: Uri): Long {
        val meta = MediaMetadataRetriever()
        meta.setDataSource(requireContext(), uri)
        val d = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
        meta.release()
        return d

    }

    private fun prepareToPlay() {
        val clip = MediaItem.ClippingConfiguration.Builder()
        if (trim.maxDuration > 0)
            clip.setEndPositionMs(trim.maxDuration)

        val media = MediaItem.Builder()
            .setUri(uri)
            .setClippingConfiguration(clip.build())
            .build()

        player.addMediaItem(media)
        player.prepare()
    }


    companion object {
        private const val TAG = "TrimFragment"
    }
}