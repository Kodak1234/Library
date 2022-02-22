package com.ume.trimview.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ume.adapter.DelegateHolder
import com.ume.adapter.delegate.AdapterDelegate
import com.ume.trimview.R
import com.ume.trimview.data.Frame

class FrameDelegate : AdapterDelegate() {

    override fun createHolder(
        type: Int,
        parent: ViewGroup,
        inflater: LayoutInflater
    ): DelegateHolder {
        return FrameHolder(inflater.inflate(R.layout.row_frame, parent, false))
    }

    override fun types(): IntArray = intArrayOf(0)

    class FrameHolder(v: View) : DelegateHolder(v) {
        override fun bind() {
            super.bind()
            val frame = item<Frame>()
            Glide.with(itemView)
                .load(frame)
                .into(itemView as ImageView)
        }
    }
}