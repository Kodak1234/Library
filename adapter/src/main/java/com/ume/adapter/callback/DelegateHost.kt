package com.ume.adapter.callback

import android.os.Bundle
import com.ume.adapter.delegate.AdapterDelegate

interface DelegateHost {
    fun addDelegate(vararg delegates: AdapterDelegate)

    fun getDelegate(type: Int): AdapterDelegate?

    fun onSave(bundle: Bundle?)

    fun onRestore(bundle: Bundle?)
}