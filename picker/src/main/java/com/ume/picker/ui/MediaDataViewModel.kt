package com.ume.picker.ui

import android.app.Application
import android.database.ContentObserver
import android.database.Cursor
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaDataViewModel(private val app: Application) : AndroidViewModel(app) {
    val cursor = MutableLiveData<Cursor?>()
    private var pCursor: Cursor? = null
    private val observer = CursorObserver()

    override fun onCleared() {
        super.onCleared()
        pCursor?.unregisterContentObserver(observer)
        pCursor?.close()
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            val c = app.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                arrayOf(FileColumns.MIME_TYPE, FileColumns.DATA, FileColumns.DISPLAY_NAME),
                null, null, null
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
}