package com.ume.picker.data

import com.ume.adapter.AdapterItem

data class MediaItem(
    val name: String,
    val data: String,
) : AdapterItem() {

    object Type {
        const val VIDEO = 1
        const val IMAGE = 2
        const val AUDIO = 3
        const val UNKNOWN = 4
    }
}