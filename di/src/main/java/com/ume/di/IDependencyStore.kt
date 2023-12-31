package com.ume.socialstat.di

import kotlin.reflect.KClass

interface IDependencyStore {

    fun register(dependency: Any, scope: Any?, klass: KClass<*>)

    fun unregister(klass: KClass<*>, scope: Any?)

    fun getDependency(klass: KClass<*>, scope: Any?): Any?
}