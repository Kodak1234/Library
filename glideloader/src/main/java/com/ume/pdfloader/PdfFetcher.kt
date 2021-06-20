package com.ume.pdfloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.PNG
import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


class PdfFetcher(private val path: Uri, private val context: Context) :
    DataFetcher<InputStream> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream?>) {
        try {
            val descriptor = context.contentResolver
                .openFileDescriptor(path, "r")
            callback.onDataReady(compressPdf(descriptor!!))
        } catch (e: Exception) {
            callback.onLoadFailed(e)
        }
    }

    override fun cleanup() {}
    override fun cancel() {}

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

    private fun compressPdf(descriptor: ParcelFileDescriptor): ByteArrayInputStream {
        val renderer = PdfRenderer(descriptor)
        val pageCount = renderer.pageCount
        val page = renderer.openPage(pageCount shr 1)
        val bitmap: Bitmap = createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, RENDER_MODE_FOR_DISPLAY)
        page.close()
        renderer.close()
        descriptor.close()
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(PNG, 100, outputStream)
        bitmap.recycle()
        return ByteArrayInputStream(outputStream.toByteArray())
    }
}