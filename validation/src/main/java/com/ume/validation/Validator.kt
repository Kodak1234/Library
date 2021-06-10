package com.ume.validation

import android.view.View

abstract class Validator {
    var id: Any = -1
        internal set

    /**
     * Perform validation
     *
     * @return true if valid else false
     */
    abstract suspend fun validate(): Boolean

    interface Factory {
        fun createValidator(id: Any, view: View, validation: Validation): Validator
    }
}