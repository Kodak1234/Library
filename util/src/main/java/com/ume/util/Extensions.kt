package com.ume.util

import android.content.res.Resources
import android.util.TypedValue

fun Resources.dp(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    value, displayMetrics
)

fun Resources.dp(value: Int) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    value.toFloat(), displayMetrics
).toInt()