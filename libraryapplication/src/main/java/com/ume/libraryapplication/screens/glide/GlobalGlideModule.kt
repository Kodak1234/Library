package com.ume.libraryapplication.screens.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class GlobalGlideModule : AppGlideModule() {

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

}