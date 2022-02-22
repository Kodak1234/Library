package com.ume.trimview.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.LibraryGlideModule
import com.ume.trimview.data.Frame
import java.io.InputStream

@GlideModule
class FrameModule : LibraryGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        registry.prepend(
            Frame::class.java,
            InputStream::class.java,
            FrameFactory(context.applicationContext)
        )
    }
}