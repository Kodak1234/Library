package com.ume.guidedtour.impl

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.ume.guidedtour.ISceneManager
import com.ume.guidedtour.Scene
import java.util.*

/**
 * Executes scenes as the becomes ready. No particular order
 */
class AsynchronousSceneManager(vararg scenes: Scene) : ISceneManager {

    private var started = false
    private val scenes = LinkedList(scenes.toList())
    private val scenesReady = LinkedList<Scene>()
    private var scene: Scene? = null
    private val handler: SceneHandler
        get() = SceneHandler(this)

    override fun beginTour(delay: Long) {
        if (started) throw IllegalStateException("Cannot call beginTour more than once")
        started = true
        handler.sendEmptyMessageDelayed(BEGIN, delay)
    }

    private fun begin() {
        while (scenes.isNotEmpty()) {
            val scene = scenes.poll()!!
            val ready: () -> Unit = {
                scenesReady.add(scene)
                handler.sendEmptyMessage(TOUR)
            }
            if (scene.dictator.canTour())
                scene.watcher.watchScene(ready)
        }
    }

    private fun onSceneReady() {
        if (scene == null && scenesReady.isNotEmpty()) {
            scene = scenesReady.poll()
            scene!!.guide.beginTour {
                scene = null
                handler.sendEmptyMessage(TOUR)
            }
        }
    }

    private class SceneHandler(private val mn: AsynchronousSceneManager) :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                BEGIN -> mn.begin()
                TOUR -> mn.onSceneReady()
            }
        }
    }

    companion object {
        private const val BEGIN = 1
        private const val TOUR = 2
    }
}