package com.ume.bottomsheet

interface IBottomSheet {

    val scrollId: Int
    val liftId: Int
    val cancelable: Boolean

    fun getCornerRadius(): Float

    fun getLiftElevation(): Float

    fun close()
}