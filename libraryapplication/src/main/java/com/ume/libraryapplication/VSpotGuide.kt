package com.ume.libraryapplication

import android.view.View
import com.developer.spoti.vspoti.VSpotView
import com.example.guidedtour.IDictator
import com.example.guidedtour.IGuide
import com.example.guidedtour.SceneManager

class VSpotGuide(
    private val view: View,
    private val title: String,
    private val msg: String,
) : IGuide {

    override fun beginTour(manager: SceneManager, dictator: IDictator) {
        VSpotView.Builder(view.context)
            .setTitle(title)
            .setContentText(msg)
            .setTargetView(view)
            .setDismissType(VSpotView.DismissType.anywhere)
            .setVSpotListener {
                dictator.commitTour()
                manager.nextTour()
            }
            .build()
            .show();
    }
}