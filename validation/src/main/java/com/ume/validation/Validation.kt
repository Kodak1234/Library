package com.ume.validation

import android.view.View
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

    @Suppress("UNCHECKED_CAST")
    fun getValidator(view: View): List<Validator> {
        val validators = view.getTag(R.id.validator) as ArrayList<Validator>?
        return if (validators == null) emptyList() else Collections.unmodifiableList(validators)
    }

    @Suppress("UNCHECKED_CAST")
    fun attachValidator(validator: Validator, view: View) {
        var list = view.getTag(R.id.validator) as ArrayList<Validator>?
        if (list == null) {
            list = ArrayList()
            view.setTag(R.id.validator, list)
        }
        list.add(validator)
        validators.add(validator)
    }

}