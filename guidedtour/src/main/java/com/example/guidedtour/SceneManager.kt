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
    private var started = false

    fun beginTour() {
        if (started) throw IllegalStateException("Cannot call beginTour() more than once")
        started = true
        handler.sendEmptyMessage(NEXT)
    }

    private fun dispatchTour() {
        scene!!.guide.beginTour {
            scene!!.dictator.commitTour()
            handler.sendEmptyMessage(NEXT)
        }
    }

    private fun nextTour() {
        do {
            scene = nextScene()
            val sc = scene
            if (sc != null && sc.dictator.canTour()) {
                sc.watcher.watchScene {
                    handler.sendEmptyMessage(TOUR)
                }
                break
            }
        } while (scene != null)
    }

    private fun nextScene(): Scene? = scenes.poll()

    private class SceneHandler(private val mn: SceneManager) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                NEXT -> mn.nextTour()
                TOUR -> mn.dispatchTour()
            }
        }
    }

    companion object {
        private const val NEXT = 1
        private const val TOUR = 2
    }
}