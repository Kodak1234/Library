package com.ume.trimview.data

import android.net.Uri

data class Frame(val time: Long, val uri: Uri) {
    override fun toString(): String {
        return "Frame(time=$time, uri=$uri)"
    }
}