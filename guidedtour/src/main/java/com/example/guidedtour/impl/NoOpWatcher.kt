package com.example.guidedtour.impl

import com.example.guidedtour.ISceneWatcher

class NoOpWatcher : ISceneWatcher {
    override fun watchScene(notify: () -> Unit) {
        notify()
    }
}