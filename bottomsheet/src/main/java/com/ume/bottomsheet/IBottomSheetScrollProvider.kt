package com.ume.bottomsheet

import android.view.View

interface IBottomSheetScrollProvider {

    val scrollingView: View?
        get() = null

    val liftOnScroll: View?
        get() = null
}