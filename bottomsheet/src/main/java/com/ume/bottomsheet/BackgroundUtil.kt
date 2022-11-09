package com.ume.bottomsheet

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.view.ViewCompat
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable

internal object BackgroundUtil {

    fun setBackground(
        view: View?, radius: Float,
        color: Int? = null
    ) {
        if (view != null) {
            val bg = view.background as? MaterialShapeDrawable ?: MaterialShapeDrawable()
            bg.shapeAppearanceModel = bg.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                .setTopRightCorner(CornerFamily.ROUNDED, radius)
                .build()

            val fill = color ?: getColor(view)
            if (fill != null)
                bg.fillColor = ColorStateList.valueOf(fill)

            if (view.background != bg) {
                ViewCompat.setBackground(view, bg)
            }
        }
    }

    fun getBackground(view: View?): MaterialShapeDrawable? {
        return view?.background as? MaterialShapeDrawable?
    }

    fun getColor(view: View): Int? {
        return (view.background as? ColorDrawable)?.color
    }
}