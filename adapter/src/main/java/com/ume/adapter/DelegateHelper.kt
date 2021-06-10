package com.ume.adapter

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import androidx.lifecycle.LifecycleObserver
import com.ume.adapter.callback.DelegateHost
import com.ume.adapter.callback.StateListener
import com.ume.adapter.delegate.AdapterDelegate

class DelegateHelper : StateListener, DelegateHost, LifecycleObserver {
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

    override fun onStart() {
        for (delegate in list) {
            delegate.onStart()
        }
    }

    override fun onStop() {
        for (delegate in list) {
            delegate.onStop()
        }
    }

    override fun onCreate() {
        for (delegate in list) {
            delegate.onCreate()
        }
    }

    override fun onDestroy() {
        for (delegate in list) {
            delegate.onDestroy()
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

    companion object {
        private const val STATE = "DelegateHelper:STATE"
    }
}