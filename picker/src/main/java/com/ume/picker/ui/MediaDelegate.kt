package com.ume.picker.ui

import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bumptech.glide.Glide
import com.ume.adapter.DelegateHolder
import com.ume.adapter.callback.AdapterItemListener
import com.ume.adapter.delegate.AdapterDelegate
import com.ume.picker.R
import com.ume.picker.data.MediaItem
import com.ume.picker.data.MediaItem.Companion.getDuration
import com.ume.picker.data.MediaItem.Type
import com.ume.selection.SelectionHelper
import com.ume.util.dp
import com.ume.util.sdkAtLeast
import java.io.File

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
        private val duration = v.findViewById<TextView>(R.id.duration)
        private val info = v.findViewById<View>(R.id.info)
        private val select = v.findViewById<ImageView>(R.id.select)

        init {
            when (type) {
                Type.IMAGE, Type.VIDEO -> {
                    info.isGone = type == Type.IMAGE
                    duration.isGone = info.isGone
                }
                Type.AUDIO, Type.UNKNOWN -> {
                    icon.isGone = true
                    duration.isGone = type == Type.UNKNOWN
                    img.setPadding(img.resources.dp(12))
                    img.setImageResource(
                        if (type == Type.UNKNOWN)
                            R.drawable.ic_file
                        else R.drawable.ic_audio
                    )
                }
            }

            itemView.setOnClickListener {
                selector.select(bindingAdapterPosition, false)
                listener.onAdapterItemClicked(this, it)
                val item = item<MediaItem>()
                selector.checkItem({
                    select.isInvisible = !this
                    if (this) {
                        item.runAnim = false
                        select.setImageResource(R.drawable.animated_check)
                        if (sdkAtLeast(Build.VERSION_CODES.M)) {
                            val d = select.drawable as AnimatedVectorDrawable
                            d.registerAnimationCallback(AnimationCallback23(item, d))
                            d.start()
                        } else {
                            val d = (select.drawable as AnimatedVectorDrawableCompat)
                            d.registerAnimationCallback(AnimationCallbackSupport(item, d))
                            d.start()
                        }

                    }
                }, bindingAdapterPosition)
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

            if (duration.isVisible)
                item.getDuration(duration)

            selector.checkItem({
                select.isInvisible = !this
                if (select.isVisible && !item.runAnim)
                    select.setImageResource(R.drawable.ic_check)
            }, bindingAdapterPosition)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private class AnimationCallback23(
        private val item: MediaItem,
        private val d: AnimatedVectorDrawable
    ) : Animatable2.AnimationCallback() {
        override fun onAnimationEnd(drawable: Drawable?) {
            super.onAnimationEnd(drawable)
            item.runAnim = true
            d.unregisterAnimationCallback(this)
        }
    }

    private class AnimationCallbackSupport(
        private val item: MediaItem,
        private val d: AnimatedVectorDrawableCompat
    ) : Animatable2Compat.AnimationCallback() {
        override fun onAnimationEnd(drawable: Drawable?) {
            super.onAnimationEnd(drawable)
            item.runAnim = true
            d.unregisterAnimationCallback(this)
        }
    }
}