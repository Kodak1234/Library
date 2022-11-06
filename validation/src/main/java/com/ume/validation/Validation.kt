package com.ume.validation

import java.util.*

class Validation {

    private val validators = LinkedList<Validator>()

    suspend fun validate(): Boolean {
        for (validator in validators) {
            if (!validator.validate()) {
                validator.setError()
                return false
            }
        }

        return true
    }

    fun getValidators(): List<Validator> = Collections.unmodifiableList(validators)

    @Suppress("UNCHECKED_CAST")
    fun addValidator(validator: Validator) {
        validators.add(validator)
    }

}