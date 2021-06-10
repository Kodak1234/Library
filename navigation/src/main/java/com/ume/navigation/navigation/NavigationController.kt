package com.ume.navigation.navigation

interface NavigationController {
    fun select(id: Int, run: () -> Unit = {})

    fun push(id: Int)

    fun pop()

    fun getId(): Int;
}
