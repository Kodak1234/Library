package com.ume.guidedtour

interface IGuide {
    fun beginTour(notify: () -> Unit)
}