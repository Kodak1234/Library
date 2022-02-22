package com.ume.trimview.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.PNG
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.OPTION_CLOSEST
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.ume.trimview.data.Frame
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


class FrameFetcher(private val frame: Frame, private val context: Context) :
    DataFetcher<InputStream> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream?>) {
        val meta = MediaMetadataRetriever()
        try {
            meta.setDataSource(context, frame.uri)
            callback.onDataReady(compress(meta.getFrameAtTime(frame.time, OPTION_CLOSEST)!!))
        } catch (e: Exception) {
            callback.onLoadFailed(e)
        } finally {
            meta.release()
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

    private fun compress(bitmap: Bitmap): ByteArrayInputStream {
        val out = ByteArrayOutputStream()
        bitmap.compress(PNG, 100, out)
        bitmap.recycle()
        return ByteArrayInputStream(out.toByteArray())
    }
}