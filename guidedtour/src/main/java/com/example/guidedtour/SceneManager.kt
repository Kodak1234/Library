package com.example.guidedtour

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.util.*

class SceneManager(vararg args: Scene) {
    private val scenes = LinkedList(args.toList())
    private var scene: Scene? = null
    private val handler: SceneHandler
        get() = SceneHandler(this)

    fun notifySceneChange() {
        handler.sendEmptyMessage(TOUR)
    }

    fun nextTour() {
        handler.sendEmptyMessage(NEXT)
    }

    private fun dispatchTour() {
        scene!!.guide.beginTour(this, scene!!.dictator)
    }

    private fun startNextTour() {
        do {
            scene = nextScene()
            val sc = scene
            if (sc != null && sc.dictator.canTour()) {
                sc.watcher.watchScene(this)
                break
            }
        } while (scene != null)
    }

    private fun nextScene(): Scene? = scenes.poll()

    private class SceneHandler(private val mn: SceneManager) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                NEXT -> mn.startNextTour()
                TOUR -> mn.dispatchTour()
            }
        }
    }

    companion object {
        private const val NEXT = 1
        private const val TOUR = 2
    }
}