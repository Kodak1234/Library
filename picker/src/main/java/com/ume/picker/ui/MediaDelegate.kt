package com.ume.picker.ui

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bumptech.glide.Glide
import com.ume.adapter.DelegateHolder
import com.ume.adapter.callback.AdapterItemListener
import com.ume.adapter.delegate.AdapterDelegate
import com.ume.picker.R
import com.ume.picker.data.MediaItem
import com.ume.picker.data.MediaItem.Type
import com.ume.selection.SelectionHelper
import com.ume.util.dp
import com.ume.util.sdkAtLeast

class MediaDelegate(
    private val selector: SelectionHelper,
    private val listener: AdapterItemListener
) : AdapterDelegate() {

    override fun createHolder(
        type: Int,
        parent: ViewGroup,
        inflater: LayoutInflater
    ): DelegateHolder {
        return Holder(inflater.inflate(R.layout.row_media, parent, false), type)
    }

    override fun types(): IntArray = intArrayOf(Type.IMAGE, Type.VIDEO, Type.AUDIO, Type.UNKNOWN)

    private inner class Holder(v: View, type: Int) : DelegateHolder(v) {
        private val img = v.findViewById<ImageView>(R.id.img)
        private val name = v.findViewById<TextView>(R.id.name)
        private val icon = v.findViewById<ImageView>(R.id.icon)
        private val select = v.findViewById<ImageView>(R.id.select)

        init {
            when (type) {
                Type.IMAGE, Type.VIDEO -> {
                    name.isGone = true
                    icon.isGone = type == Type.IMAGE
                }
                Type.AUDIO, Type.UNKNOWN -> {
                    icon.isGone = true
                    img.setPadding(img.resources.dp(12))
                    img.setImageResource(
                        if (type == Type.UNKNOWN)
                            R.drawable.ic_file
                        else R.drawable.ic_audio
                    )
                }
            }

            itemView.setOnClickListener {
                selector.select(bindingAdapterPosition, true)
                listener.onAdapterItemClicked(this, it)
            }
        }

        override fun bind() {
            val item = item<MediaItem>()
            when (item.type) {
                Type.IMAGE, Type.VIDEO -> {
                    Glide.with(img)
                        .load(item.data)
                        .into(img)
                }
                else -> {
                    name.text = item.name
                }
            }

            selector.checkItem({
                select.isInvisible = !this
                if (select.isVisible) {
                    if (item.runAnim) {
                        item.runAnim = false
                        select.setImageResource(R.drawable.animated_check)
                        if (sdkAtLeast(Build.VERSION_CODES.LOLLIPOP))
                            (select.drawable as AnimatedVectorDrawable).start()
                        else
                            (select.drawable as AnimatedVectorDrawableCompat).start()
                    } else
                        select.setImageResource(R.drawable.ic_check)
                }
            }, bindingAdapterPosition)
        }
    }
}