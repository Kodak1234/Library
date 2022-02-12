package com.ume.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ume.adapter.callback.AdapterSource
import com.ume.adapter.delegate.AdapterDelegate

open class DelegateHolder(v: View) : ViewHolder(v) {
    lateinit var delegate: AdapterDelegate
        internal set
    lateinit var source: AdapterSource<*>
        internal set
    lateinit var adapter: DelegateAdapter
        internal set

    @Suppress("UNCHECKED_CAST")
    fun <E> item(): E = source[bindingAdapterPosition] as E

    open fun bind() {}

}