package com.ume.picker.data

import android.annotation.SuppressLint
import android.database.Cursor
import android.provider.MediaStore.Files.FileColumns
import androidx.recyclerview.widget.RecyclerView

class MediaDataSource(adapter: RecyclerView.Adapter<*>) : CursorDataSource<MediaItem>(adapter) {

    @SuppressLint("Range")
    override fun parse(cursor: Cursor): MediaItem {
        val name = cursor.getString(cursor.getColumnIndex(FileColumns.DISPLAY_NAME))
        val mime = cursor.getString(cursor.getColumnIndex(FileColumns.MIME_TYPE))
        val data = cursor.getString(cursor.getColumnIndex(FileColumns.DATA))
        return MediaItem(name, data).apply {
            type = when {
                mime == null -> MediaItem.Type.UNKNOWN
                mime.contains("video") -> MediaItem.Type.VIDEO
                mime.contains("image") -> MediaItem.Type.IMAGE
                mime.contains("audio") -> MediaItem.Type.AUDIO
                else -> MediaItem.Type.UNKNOWN
            }
        }
    }

    override fun type(index: Int): Int = get(index)!!.type
}