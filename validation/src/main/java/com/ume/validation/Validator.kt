package com.ume.validation

import android.view.View

abstract class Validator {


    /**
     * Perform validation
     *
     * @return true if valid else false
     */
    abstract suspend fun validate(): Boolean
}