package com.ume.picker.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.net.Uri
import android.os.Parcelable
import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import com.ume.adapter.AdapterItem
import com.ume.util.toDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaItem(
    val name: String,
    val data: Uri,
    val mime: String,
    var duration: Long = -1L
) : AdapterItem(), Parcelable {

    @IgnoredOnParcel
    internal var runAnim = true


    companion object {
        private val metaScope = CoroutineScope(Dispatchers.IO)
        private val jobs = mutableMapOf<MediaItem, MetaDataWorker>()
        fun MediaItem.getDuration(textView: TextView) {
            when {
                duration != -1L -> textView.text = duration.toDuration()
                jobs[this] != null -> jobs[this]!!.updateView(textView)
                else -> {
                    val job = MetaDataWorker(this, textView.context.applicationContext)
                    job.updateView(textView)
                    jobs[this] = job
                    metaScope.launch { job.execute() }
                }
            }
        }

        private fun MediaItem.clearJob() {
            jobs.remove(this)
        }
    }

    private class MetaDataWorker(
        private val item: MediaItem,
        private val context: Context
    ) :
        View.OnAttachStateChangeListener {
        var viewRef: TextView? = null

        override fun onViewAttachedToWindow(p0: View?) {

        }

        override fun onViewDetachedFromWindow(p0: View?) {
            viewRef = null
        }

        fun updateView(view: TextView?) {
            if (viewRef != view) {
                viewRef?.removeOnAttachStateChangeListener(this)
                viewRef = view
                view?.addOnAttachStateChangeListener(this)
            }
        }

        suspend fun execute() {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, item.data)
            val d = retriever.extractMetadata(METADATA_KEY_DURATION)?.toLong()
            retriever.release()
            item.duration = d ?: 0
            withContext(Dispatchers.Main) {
                viewRef?.text = item.duration.toDuration()
                item.clearJob()
                updateView(null)
            }
        }
    }

    object Type {
        const val IMAGE = 1
        const val VIDEO = 1 shl 1
        const val AUDIO = 1 shl 2
        const val UNKNOWN = 1 shl 3
    }

    override fun toString(): String {
        return "MediaItem(name='$name', data='$data', mime='$mime', duration=$duration, type=$type)"
    }
}