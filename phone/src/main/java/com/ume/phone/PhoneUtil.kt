package com.ume.phone

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyManager
import java.util.*

class PhoneUtil(private val app: Context) {

    private val codes = Properties()

    init {
        app.resources
            .openRawResource(R.raw.codes)
            .use { input -> codes.load(input) }
    }

    fun getCode(code: String): String? {
        return codes[code.uppercase()] as? String?
    }

    fun getCode(): String? {
        val mn = app.getSystemService(Context.TELEPHONY_SERVICE) as
                TelephonyManager

        var code = mn.simCountryIso
        if (code.isBlank())
            code = mn.networkCountryIso
        if (code.isBlank())
            code = Locale.getDefault().country

        return getCode(code)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var phone: PhoneUtil? = null

        fun create(context: Context): PhoneUtil {
            synchronized(this) {
                if (phone == null) {
                    phone = PhoneUtil(context.applicationContext)
                }
            }
            return phone!!
        }
    }
}