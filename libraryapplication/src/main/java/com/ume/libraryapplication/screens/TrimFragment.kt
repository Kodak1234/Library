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
import com.google.android.exoplayer2.ui.PlayerControlView
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
        playButton.setOnClickListener {
            if (player.isPlaying)
                player.pause()
            else {
                if (player.playbackState == Player.STATE_ENDED)
                    player.seekToDefaultPosition()
                player.playWhenReady = true
            }
        }
        playerView.player = player

        if (uri == null)
            launcher.launch(arrayOf("video/*"))
        else
            prepareToPlay()

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

    override fun onLeftHandleReleased(duration: Long, handle: View) {
        super.onLeftHandleReleased(duration, handle)
        val clip = MediaItem.ClippingConfiguration.Builder()
            .setStartPositionMs(duration)
            .setEndPositionMs(trim.getEndDuration())
            .build()

        val media = MediaItem.Builder()
            .setUri(uri)
            .setClippingConfiguration(clip)
            .build()
        setMedia(media)
    }

    override fun onRightHandleReleased(duration: Long, handle: View) {
        super.onRightHandleReleased(duration, handle)
        val clip = MediaItem.ClippingConfiguration.Builder()
            .setStartPositionMs(trim.getStartDuration())
            .setEndPositionMs(duration)
            .build()

        val media = MediaItem.Builder()
            .setUri(uri)
            .setClippingConfiguration(clip)
            .build()
        setMedia(media)
    }

    override fun onSeekHandleReleased(duration: Long, handle: View) {
        super.onSeekHandleReleased(duration, handle)
        player.seekTo(duration)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("URI", uri)
    }

    private fun setMedia(media: MediaItem) {
        player.addMediaItem(media)
        player.removeMediaItem(0)
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
        trim.setUri(uri!!, getDuration(uri!!))

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