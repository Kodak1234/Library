package com.ume.validation

import java.util.*

class Validation {

    private val validators = LinkedList<Validator>()

    suspend fun validate(): Boolean {
        var valid = true
        for (validator in validators) {
            valid = validator.validate() && valid
        }

        return valid
    }

    fun getValidators(): List<Validator> = Collections.unmodifiableList(validators)

    @Suppress("UNCHECKED_CAST")
    fun addValidator(validator: Validator) {
        validators.add(validator)
    }

}