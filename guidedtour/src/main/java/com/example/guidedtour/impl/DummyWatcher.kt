package com.example.guidedtour.impl

import com.example.guidedtour.ISceneWatcher
import com.example.guidedtour.SceneManager

class DummyWatcher : ISceneWatcher {
    override fun watchScene(manager: SceneManager) {
        manager.notifySceneChange()
    }
}