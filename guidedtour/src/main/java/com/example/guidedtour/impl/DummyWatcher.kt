package com.example.guidedtour.impl

import com.example.guidedtour.ISceneWatcher

class DummyWatcher : ISceneWatcher {
    override fun watchScene(notify: () -> Unit) {
        notify()
    }
}