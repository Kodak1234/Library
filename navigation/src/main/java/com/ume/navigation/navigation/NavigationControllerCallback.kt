package com.ume.navigation.navigation

import androidx.fragment.app.Fragment

interface NavigationControllerCallback {

    fun getFragmentTag(id: Int): String

    fun getFragment(id: Int): Fragment

    fun getAnimation(tag: String, enter: Boolean): Int = 0
}