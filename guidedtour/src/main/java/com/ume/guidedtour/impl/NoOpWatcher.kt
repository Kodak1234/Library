package com.ume.guidedtour.impl

import com.ume.guidedtour.ISceneWatcher

class NoOpWatcher : ISceneWatcher {
    override fun watchScene(notify: () -> Unit) {
        notify()
    }
}