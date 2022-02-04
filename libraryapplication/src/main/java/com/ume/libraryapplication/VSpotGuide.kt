package com.ume.libraryapplication

import android.view.View
import com.developer.spoti.vspoti.VSpotView
import com.example.guidedtour.IGuide

class VSpotGuide(
    private val view: View,
    private val title: String,
    private val msg: String,
) : IGuide {

    override fun beginTour(notify: () -> Unit) {
        VSpotView.Builder(view.context)
            .setTitle(title)
            .setContentText(msg)
            .setTargetView(view)
            .setDismissType(VSpotView.DismissType.anywhere)
            .setVSpotListener {
                notify()
            }
            .build()
            .show();
    }
}