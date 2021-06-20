package com.ume.pdfloader

import android.content.Context
import android.net.Uri
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

class PdfLoader(private val context: Context) :
    ModelLoader<Uri, InputStream> {
    override fun buildLoadData(
        s: Uri, width: Int, height: Int,
        options: Options
    ): LoadData<InputStream> {
        return LoadData(ObjectKey(s), PdfFetcher(s, context))
    }

    override fun handles(s: Uri): Boolean {
        return uriType(s).contains("pdf")
    }

    private fun uriType(s: Uri): String {
        val type = context.contentResolver.getType(s)
        return type ?: ""
    }
}