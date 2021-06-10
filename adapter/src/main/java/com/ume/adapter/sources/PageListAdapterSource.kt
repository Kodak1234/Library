package com.ume.adapter.sources

import androidx.lifecycle.Lifecycle
import androidx.paging.*
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.ume.adapter.AdapterItem
import com.ume.adapter.callback.AdapterSource

class PageListAdapterSource<E : AdapterItem>(
    adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
    callback: DiffUtil.ItemCallback<E>,
    updateCallback: ListUpdateCallback = AdapterListUpdateCallback(adapter)
) : AdapterSource<E> {

    private val differ: AsyncPagingDataDiffer<E> = AsyncPagingDataDiffer(callback, updateCallback)

    fun submit(list: PagingData<E>, lifecycle: Lifecycle) {
        differ.submitData(lifecycle, list)
    }

    suspend fun submit(list: PagingData<E>) {
        differ.submitData(list)
    }

    fun addLoadStateListener(listener: (CombinedLoadStates) -> Unit) {
        differ.addLoadStateListener(listener)
    }

    fun removeLoadStateListener(listener: (CombinedLoadStates) -> Unit) {
        differ.removeLoadStateListener(listener)
    }

    override fun size(): Int = differ.itemCount

    override fun get(index: Int): E? = differ.getItem(index)

    override fun type(index: Int): Int = get(index)!!.type
}