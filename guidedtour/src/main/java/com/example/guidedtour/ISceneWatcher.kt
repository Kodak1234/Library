package com.example.guidedtour

interface ISceneWatcher {

    fun watchScene(notify: () -> Unit)
}