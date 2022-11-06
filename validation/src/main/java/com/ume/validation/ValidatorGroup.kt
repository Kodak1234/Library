package com.ume.validation

class ValidatorGroup(
    private vararg val validators: Validator
) : Validator() {

    override suspend fun validate(): Boolean {
        var valid = true
        for (validator in validators) {
            valid = validator.validate() && valid
        }

        return valid
    }

    override fun setError() {
        for (validator in validators) {
            validator.setError()
        }
    }
}