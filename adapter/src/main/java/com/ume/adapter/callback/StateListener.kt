package com.ume.adapter.callback

import androidx.lifecycle.Lifecycle.Event.*
import androidx.lifecycle.OnLifecycleEvent

interface StateListener {
    @OnLifecycleEvent(ON_START)
    fun onStart(){}
    @OnLifecycleEvent(ON_STOP)
    fun onStop(){}
    @OnLifecycleEvent(ON_CREATE)
    fun onCreate(){}
    @OnLifecycleEvent(ON_DESTROY)
    fun onDestroy(){}
}