package com.example.guidedtour

interface IGuide {
    fun beginTour(notify: () -> Unit)
}