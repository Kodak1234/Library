package com.ume.adapter

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ume.adapter.callback.DelegateHost
import com.ume.adapter.delegate.AdapterDelegate

class DelegateHelper : DefaultLifecycleObserver, DelegateHost, LifecycleObserver {
    private val delegates = SparseArray<AdapterDelegate>()
    private val list = ArrayList<AdapterDelegate>()

    override fun onSave(bundle: Bundle?) {
        bundle?.run {
            val states = SparseArray<Parcelable?>()
            for (delegate in list) {
                states.put(delegate.types()[0], delegate.onSaveState())
            }
            putSparseParcelableArray(STATE, states)
        }
    }

    override fun onRestore(bundle: Bundle?) {
        bundle?.run {
            val states: SparseArray<Parcelable>? = getSparseParcelableArray(STATE)
            for (i in 0 until (states?.size() ?: 0))
                delegates[states!!.keyAt(i)].onRestoreState(states.valueAt(i))
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        for (delegate in list) {
            delegate.onStart(owner)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        for (delegate in list) {
            delegate.onStop(owner)
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        for (delegate in list) {
            delegate.onCreate(owner)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        owner.lifecycle.removeObserver(this)
        for (delegate in list) {
            delegate.onDestroy(owner)
        }
    }

    override fun addDelegate(vararg delegates: AdapterDelegate) {
        for (delegate in delegates) {
            list += delegate
            val types = delegate.types()
            for (type in types)
                this.delegates.put(type, delegate)
        }
    }

    override fun getDelegate(type: Int): AdapterDelegate? {
        return delegates[type]
    }

    override fun observe(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    companion object {
        private const val STATE = "DelegateHelper:STATE"
    }
}