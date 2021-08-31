package com.ume.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.ume.adapter.callback.AdapterSource
import com.ume.adapter.callback.DelegateHost
import com.ume.adapter.delegate.AdapterDelegate

class DelegateAdapter(ctx: Context, lifecycle: Lifecycle? = null) : Adapter<DelegateHolder>(),
    DelegateHost {
    var source: AdapterSource<*>? = null
        set(value) {
            field = value
            if (source != null)
                notifyDataSetChanged()
        }

    private val delegateHelper = DelegateHelper()
    private val inflater = LayoutInflater.from(ctx)

    init {
        lifecycle?.addObserver(delegateHelper)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DelegateHolder {
        val delegate = delegateHelper.getDelegate(viewType)
            ?: throw IllegalArgumentException("no delegate to handle viewType = $viewType")
        return delegate.createHolder(viewType, parent, inflater)
            .apply {
                this.adapter = this@DelegateAdapter
                this.delegate = delegate
                this.source = this@DelegateAdapter.source!!
            }
    }

    override fun getItemCount(): Int = source?.size() ?: 0

    override fun getItemViewType(position: Int): Int = source!!.type(position)

    override fun onBindViewHolder(holder: DelegateHolder, position: Int) {
        holder.delegate.bindHolder(holder, position)
    }

    override fun onViewDetachedFromWindow(holder: DelegateHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.delegate.detachedFromWindow(holder)
    }

    override fun onViewAttachedToWindow(holder: DelegateHolder) {
        super.onViewAttachedToWindow(holder)
        holder.delegate.attachedToWindow(holder)
    }

    override fun addDelegate(vararg delegates: AdapterDelegate) {
        delegateHelper.addDelegate(*delegates)
    }

    override fun getDelegate(type: Int): AdapterDelegate? = delegateHelper.getDelegate(type)

    override fun onSave(bundle: Bundle?) {
        delegateHelper.onSave(bundle)
    }

    override fun onRestore(bundle: Bundle?) {
        delegateHelper.onRestore(bundle)
    }

    @Suppress("UNCHECKED_CAST")
    fun <E> getItem(pos: Int): E? = source?.get(pos) as E?
}