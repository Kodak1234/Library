package com.ume.picker.data

import android.annotation.SuppressLint
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import androidx.recyclerview.widget.RecyclerView

class MediaDataSource(adapter: RecyclerView.Adapter<*>) : CursorDataSource<MediaItem>(adapter) {

    @SuppressLint("Range")
    override fun parse(cursor: Cursor): MediaItem {
        val name = cursor.getString(cursor.getColumnIndex(FileColumns.DISPLAY_NAME))
        val mime = cursor.getString(cursor.getColumnIndex(FileColumns.MIME_TYPE))
        val id = cursor.getLong(cursor.getColumnIndex(FileColumns._ID))
        val baseUri = MediaStore.Files.getContentUri("external")
        val data = ContentUris.withAppendedId(baseUri, id)
        return MediaItem(name, data, mime).apply {
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