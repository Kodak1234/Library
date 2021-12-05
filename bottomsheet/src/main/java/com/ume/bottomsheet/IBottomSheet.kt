package com.ume.bottomsheet

interface IBottomSheet {

    val scrollId: Int
    val liftId: Int

    fun getCornerRadius(): Float

    fun getLiftElevation(): Float

    fun close()
}