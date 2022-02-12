package com.ume.picker.data

import android.database.Cursor
import androidx.collection.LruCache
import androidx.recyclerview.widget.RecyclerView
import com.ume.adapter.callback.AdapterSource

abstract class CursorDataSource<E>(
    private val adapter: RecyclerView.Adapter<*>
) : AdapterSource<E> {

    private val cache = LruCache<Int, E>(100)

    var cursor: Cursor? = null
        set(value) {
            val update = value != field
            field = value
            if (update) {
                cache.evictAll()
                adapter.notifyDataSetChanged()
            }
        }

    override fun size(): Int = cursor?.count ?: 0

    override fun get(index: Int): E? {
        if (cache[index] == null) {
            cursor!!.moveToPosition(index)
            cache.put(index, parse(cursor!!))
        }

        return cache[index]
    }

    override fun type(index: Int): Int = 0

    protected abstract fun parse(cursor: Cursor): E
}