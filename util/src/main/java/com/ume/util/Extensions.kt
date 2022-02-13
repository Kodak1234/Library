package com.ume.util

import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import androidx.annotation.ChecksSdkIntAtLeast

fun Resources.dp(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    value, displayMetrics
)

fun Resources.dp(value: Int) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    value.toFloat(), displayMetrics
).toInt()


@ChecksSdkIntAtLeast(parameter = 0)
fun sdkAtLeast(sdk: Int): Boolean {
    return Build.VERSION.SDK_INT >= sdk
}