package com.ume.validation

import android.view.View
import java.util.*

class Validation(
    private val factory: Validator.Factory
) {

    private val validators = LinkedList<Validator>()

    suspend fun validate(): Boolean {
        var valid = true
        for (validator in validators) {
            valid = validator.validate() && valid
        }

        return valid
    }

    fun getValidator(id: Any): Validator? {
        for (validator in validators) {
            if (validator.id == id)
                return validator
        }

        return null
    }

    fun attachValidator(vararg pairs: Pair<Any, View>) {
        for (p in pairs) {
            val validator = factory.createValidator(p.first, p.second, this)
            validator.id = p.first
            validators.add(validator)
        }
    }

}