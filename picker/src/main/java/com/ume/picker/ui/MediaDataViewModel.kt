package com.ume.picker.ui

import android.app.Application
import android.database.ContentObserver
import android.database.Cursor
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import androidx.lifecycle.*
import com.ume.picker.data.MediaItem
import com.ume.util.BitFlag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaDataViewModel(
    private val app: Application,
    types: Int
) : AndroidViewModel(app) {
    val cursor = MutableLiveData<Cursor?>()
    private var pCursor: Cursor? = null
    private val observer = CursorObserver()
    private val mimeFilter = StringBuilder()
    private val mimeArg = mutableListOf<String>()

    init {
        val mimes = mapOf(
            MediaItem.Type.AUDIO to "audio",
            MediaItem.Type.IMAGE to "image",
            MediaItem.Type.VIDEO to "video"
        )

        val flag = BitFlag(types)
        for (e in mimes.entries) {
            if (flag.has(e.key)) {
                if (mimeFilter.isNotEmpty())
                    mimeFilter.append(",")
                mimeFilter.append("?")
                mimeArg.add(e.value)
            }
        }

        if (mimeFilter.isNotEmpty()) {
            mimeFilter.insert(0, "SUBSTR(${FileColumns.MIME_TYPE},1,5) IN (")
            mimeFilter.append(")")
        }

        if (flag.has(MediaItem.Type.UNKNOWN)) {
            if (mimeFilter.isNotEmpty())
                mimeFilter.append(" OR ")
            mimeFilter.append("SUBSTR(${FileColumns.MIME_TYPE},1,5) NOT IN (?,?,?)")
            for (value in mimes.values)
                mimeArg.add(value)
        }

    }

    override fun onCleared() {
        super.onCleared()
        pCursor?.unregisterContentObserver(observer)
        pCursor?.close()
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            val c = app.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                arrayOf(FileColumns.MIME_TYPE, FileColumns._ID, FileColumns.DISPLAY_NAME),
                mimeFilter.toString(), mimeArg.toTypedArray(), "${FileColumns.DATE_ADDED} DESC"
            )
            withContext(Dispatchers.Main) {
                cursor.value = c
                c?.registerContentObserver(observer)
                pCursor?.unregisterContentObserver(observer)
                pCursor?.close()
                pCursor = c
            }
        }
    }

    private inner class CursorObserver : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            load()
        }
    }

    class Factory(
        private val app: Application,
        private val types: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(Application::class.java, Int::class.java)
                .newInstance(app, types)
        }
    }
}