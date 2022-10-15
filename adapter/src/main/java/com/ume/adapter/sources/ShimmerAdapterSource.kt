package com.ume.adapter.sources

import android.annotation.SuppressLint
import com.ume.adapter.AdapterItem
import com.ume.adapter.DelegateAdapter
import com.ume.adapter.callback.AdapterSource

class ShimmerAdapterSource<E : AdapterItem>(
    private val shimmerType: Int,
    private val adapter: DelegateAdapter,
    val source: AdapterSource<E>,
    private val loadSize: Int,
    showShim: Boolean = true
) : AdapterSource<E> {

    var shimmer: Boolean = showShim
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            if (field != value) {
                field = value
                adapter.notifyDataSetChanged()
            }
        }

    override fun get(index: Int): E? {
        return if (shimmer) null else source[index]
    }

    override fun size(): Int {
        return if (shimmer) loadSize else source.size()
    }

    override fun type(index: Int): Int {
        return if (get(index) == null) shimmerType else source.type(index)
    }
}