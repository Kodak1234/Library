package com.ume.guidedtour

interface ISceneWatcher {

    fun watchScene(notify: () -> Unit)
}