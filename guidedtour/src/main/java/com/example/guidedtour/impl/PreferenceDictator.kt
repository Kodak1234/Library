package com.example.guidedtour.impl

import android.content.Context
import android.preference.PreferenceManager
import com.example.guidedtour.IDictator

class PreferenceDictator(context: Context, private val prefKey: String) : IDictator {
    private val mn = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    override fun canTour(): Boolean = mn.getBoolean(prefKey, true)

    override fun commitTour() {
        mn.edit().putBoolean(prefKey, false).apply()
    }
}