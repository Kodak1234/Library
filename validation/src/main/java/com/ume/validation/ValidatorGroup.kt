package com.ume.validation

class ValidatorGroup(
    private vararg val validators: Validator
) : Validator() {

    private val errors = mutableListOf<Validator>()

    override suspend fun validate(): Boolean {
        errors.clear()
        for (validator in validators) {
            if (!validator.validate()) {
                errors.add(validator)
            }
        }

        return errors.isEmpty()
    }

    override fun setError() {
        for (validator in errors) {
            validator.setError()
        }
    }
}