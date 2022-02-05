package com.ume.guidedtour.impl

import com.ume.guidedtour.IDictator

class NoOpDictator : IDictator {
    override fun canTour(): Boolean = true

    override fun commitTour() {

    }
}