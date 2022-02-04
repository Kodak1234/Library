package com.example.guidedtour.impl

import com.example.guidedtour.IDictator

class NoOpDictator : IDictator {
    override fun canTour(): Boolean = true

    override fun commitTour() {

    }
}