package com.ume.socialstat.di

inline fun <reified K> inject(scope: Any? = null): K {
    return checkNotNull(injectNullable(scope))
}

inline fun <reified K> injectNullable(scope: Any? = null): K? {
    return DependencyStore.shared.getDependency(K::class, scope) as? K
}

inline fun <reified K> unregisterDependency(scope: Any? = null) {
    DependencyStore.shared.unregister(K::class, scope)
}

inline fun <reified K : Any> registerDependency(dependency: K, scope: Any? = null) {
    DependencyStore.shared.register(dependency, scope, K::class)
}