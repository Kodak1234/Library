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
            ViewCompat.setBackground(view, MaterialShapeDrawable().apply {
                fillColor = ColorStateList.valueOf(
                    when {
                        color != null -> color
                        view.background is ColorDrawable -> (view.background as ColorDrawable).color
                        else -> MaterialColors.getColor(
                            view,
                            R.attr.colorSurface,
                            Color.WHITE
                        )
                    }
                )
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                    .setTopRightCorner(CornerFamily.ROUNDED, radius)
                    .build()
            })
        }
    }

    fun getBackground(view: View?): MaterialShapeDrawable? {
        return view?.background as? MaterialShapeDrawable?
    }
}