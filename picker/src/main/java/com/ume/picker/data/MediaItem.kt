package com.ume.picker.data

import com.ume.adapter.AdapterItem

data class MediaItem(
    val name: String,
    val data: String,
    val mime: String,
) : AdapterItem() {

    internal var runAnim = true

    object Type {
        const val IMAGE = 1
        const val VIDEO = 1 shl 1
        const val AUDIO = 1 shl 2
        const val UNKNOWN = 1 shl 4
    }
}