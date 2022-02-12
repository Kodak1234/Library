package com.ume.picker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.ume.adapter.DelegateHolder
import com.ume.adapter.delegate.AdapterDelegate
import com.ume.picker.R
import com.ume.picker.data.MediaItem
import com.ume.picker.data.MediaItem.Type

class ImageDelegate : AdapterDelegate() {

    override fun createHolder(
        type: Int,
        parent: ViewGroup,
        inflater: LayoutInflater
    ): DelegateHolder {
        return Holder(inflater.inflate(R.layout.row_media, parent, false))
    }

    override fun types(): IntArray = intArrayOf(Type.IMAGE, Type.VIDEO)

    class Holder(v: View) : DelegateHolder(v) {
        private val img = v.findViewById<ImageView>(R.id.img)

        override fun bind() {
            val item = item<MediaItem>()
            Glide.with(img)
                .load(item.data)
                .into(img)
        }
    }
}