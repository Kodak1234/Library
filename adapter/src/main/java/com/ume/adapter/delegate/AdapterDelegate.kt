package com.ume.adapter.delegate

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ume.adapter.DelegateHolder
import com.ume.adapter.callback.StateListener

abstract class AdapterDelegate : StateListener {

    open fun onSaveState(): Parcelable? {
        return null
    }

    open fun onRestoreState(state: Parcelable?) {

    }

    open fun bindHolder(holder: DelegateHolder, position: Int) {

    }

    open fun attachedToWindow(holder: DelegateHolder) {}

    open fun detachedFromWindow(holder: DelegateHolder) {}

    abstract fun createHolder(
        type: Int,
        parent: ViewGroup,
        inflater: LayoutInflater
    ): DelegateHolder

    abstract fun types(): IntArray
}