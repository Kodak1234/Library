package com.ume.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.View
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.lang.StringBuilder

fun Long.toDuration(): String {
    val H = 3600000
    val M = 60000
    val S = 1000

    var time = this

    val h = time / H
    time -= (h * H)

    val m = time / M
    time -= (m * M)

    val s = time / S

    val sb = StringBuilder()

    if (h > 0) sb.append(h).append(":")
    sb.append(String.format("%02d:%02d", m, s))

    return sb.toString()
}

fun Resources.dp(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    value, displayMetrics
)

fun Resources.dp(value: Int) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    value.toFloat(), displayMetrics
).toInt()

fun Context.hasPermission(perm: String): Boolean {
    return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
}

fun Fragment.hasPermission(perm: String): Boolean = requireContext().hasPermission(perm)

fun View.setEnable(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1f else 0.5f
}


@ChecksSdkIntAtLeast(parameter = 0)
fun sdkAtLeast(sdk: Int): Boolean {
    return Build.VERSION.SDK_INT >= sdk
}

//Get device name
fun deviceName(context: Context): String {
    return "${Build.MANUFACTURER} ${
        if (sdkAtLeast(Build.VERSION_CODES.N_MR1)) {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        } else null ?: Build.MODEL
    }".replaceFirstChar { it.titlecase() }
}