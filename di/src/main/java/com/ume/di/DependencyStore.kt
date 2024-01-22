package com.ume.di

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleOwner
import java.util.*
import kotlin.reflect.KClass

class DependencyStore : IDependencyStore {

    private val dependencies = mutableMapOf<DependencyKey, Any>()

    override fun register(dependency: Any, scope: Any?, klass: KClass<*>) {
        val key = DependencyKey(scope, klass)
        if (!dependencies.containsKey(key)) {
            dependencies[key] = dependency
            if (scope is LifecycleOwner && scope.lifecycle.currentState > State.DESTROYED) {
                val observer = AutoUnregister(dependency)
                key.observer = observer
                scope.lifecycle.addObserver(observer)
            }
        } else {
            throw IllegalArgumentException("${dependency::class.java.name} and ${klass.java.name} is already registered")
        }
    }

    override fun unregister(klass: KClass<*>, scope: Any?) {
        val key = dependencies.keys.firstOrNull { k -> k.klass == klass && k.scope == scope }
        if (key != null) {
            dependencies.remove(key)
            val observer = key.observer
            //if observer is not null then scope must be a LifeCycleOwner
            if (observer != null)
                (scope as LifecycleOwner).lifecycle.removeObserver(observer)
        }
    }

    override fun getDependency(klass: KClass<*>, scope: Any?): Any? =
        dependencies[DependencyKey(scope, klass)]

    private inner class AutoUnregister(private val dependency: Any) : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            unregister(dependency::class, owner)
        }
    }

    companion object {
        val shared by lazy { DependencyStore() }
    }

    private class DependencyKey(
        val scope: Any?,
        val klass: KClass<*>
    ) {
        var observer: DefaultLifecycleObserver? = null

        override fun hashCode(): Int {
            return Objects.hash(scope, klass)
        }

        override fun equals(other: Any?): Boolean {
            return when (other) {
                is DependencyKey -> scope == other.scope && klass == other.klass
                else -> super.equals(other)
            }
        }
    }
}