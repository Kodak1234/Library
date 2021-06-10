package com.ume.adapter

open class AdapterItem(var type: Int = 0) {

    open fun areContentsTheSame(item: AdapterItem) = this == item
}