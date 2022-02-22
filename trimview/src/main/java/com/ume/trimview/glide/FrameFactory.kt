package com.ume.trimview.glide

import android.content.Context
import android.net.Uri
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.ume.trimview.data.Frame
import java.io.InputStream

class FrameFactory(private val context: Context) :
    ModelLoaderFactory<Frame, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Frame, InputStream> {
        return FrameLoader(context)
    }

    override fun teardown() {}

}