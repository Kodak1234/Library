package com.ume.adapter.sources

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ume.adapter.AdapterItem
import com.ume.adapter.callback.AdapterSource

open class ListAdapterSource<E : AdapterItem>(
        adapter: Adapter<out ViewHolder>,
        callback: DiffUtil.ItemCallback<E>
) : AdapterSource<E> {

    private var list: List<E>? = null
    private val differ = AsyncListDiffer(adapter, callback)

    fun submit(list: List<E>) {
        this.list = list
        differ.submitList(list)
    }

    fun getList(): List<E>? = list

    override fun size(): Int = differ.currentList.size

    override fun get(index: Int): E = differ.currentList[index]

    override fun type(index: Int): Int = get(index).type
}