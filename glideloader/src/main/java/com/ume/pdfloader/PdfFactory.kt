package com.ume.pdfloader

import android.content.Context
import android.net.Uri
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import java.io.InputStream

class PdfFactory(private val context: Context) :
    ModelLoaderFactory<Uri, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Uri, InputStream> {
        return PdfLoader(context.applicationContext)
    }

    override fun teardown() {}

}