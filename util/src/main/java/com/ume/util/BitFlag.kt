package com.ume.util

class BitFlag(f: Int = 0) {
    var flag: Int = f


    fun add(flag: Int) {
        this.flag = this.flag or flag
    }

    fun has(flag: Int): Boolean = this.flag and flag == flag

    fun remove(vararg flags: Int) {
        for (flag in flags)
            this.flag = this.flag and flag.inv()
    }
}