package com.ume.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
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


@ChecksSdkIntAtLeast(parameter = 0)
fun sdkAtLeast(sdk: Int): Boolean {
    return Build.VERSION.SDK_INT >= sdk
}