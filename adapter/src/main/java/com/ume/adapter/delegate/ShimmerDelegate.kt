package com.ume.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ume.adapter.DelegateHolder

class ShimmerDelegate(
    private val layout: Int,
    vararg types: Int
) : AdapterDelegate() {

    private val type = types

    override fun createHolder(
        type: Int,
        parent: ViewGroup,
        inflater: LayoutInflater
    ): DelegateHolder {
        return DelegateHolder(inflater.inflate(layout, parent, false))
    }

    override fun types(): IntArray = type

}