package com.ume.trimview.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.ume.adapter.callback.AdapterSource
import com.ume.trimview.data.Frame

@SuppressLint("NotifyDataSetChanged")
class FrameSource(
    private val adapter: RecyclerView.Adapter<*>,
    private var itemWidth: Int,
    var uri: Uri = Uri.EMPTY,
    var duration: Long = 0
) : AdapterSource<Frame> {
    private var frames = emptyList<Frame>()
        set(value) {
            field = value
            adapter.notifyDataSetChanged()
        }

    override fun size(): Int = frames.size

    override fun get(index: Int): Frame = frames[index]

    override fun type(index: Int): Int = 0

    fun onUpdate(width: Int) {
        if (duration > 0) {
            val frameCount = width / itemWidth
            val timeStep = duration / frameCount
            val frames = mutableListOf<Frame>()
            for (i in 0 until duration step timeStep) {
                frames += Frame(i * 1000, uri)
            }
            this.frames = frames
            Log.i("FRAMES", "onUpdate: $frames")
        }
    }
}