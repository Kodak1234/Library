package com.ume.adapter.callback

interface AdapterSource<E> {

    fun size(): Int

    operator fun get(index: Int): E?

    fun type(index: Int):Int
}