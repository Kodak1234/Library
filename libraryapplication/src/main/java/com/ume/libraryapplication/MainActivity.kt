package com.ume.libraryapplication

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ume.bottomsheet.BottomSheetManager
import com.ume.guidedtour.ISceneManager
import com.ume.guidedtour.Scene
import com.ume.guidedtour.impl.AsynchronousSceneManager
import com.ume.guidedtour.impl.NoOpDictator
import com.ume.guidedtour.impl.NoOpWatcher
import com.ume.phone.PhoneUtil

class MainActivity : AppCompatActivity() {
    private lateinit var sceneMn: ISceneManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val v = findViewById<TextView>(R.id.text)
        v.text = PhoneUtil.create(this)
            .getCode()

        val sheet: ViewGroup = findViewById(R.id.container)!!
        val behavior = BottomSheetBehavior.from(sheet)
        val mn = BottomSheetManager.attach(
            this, null, sheet, behavior
        )

        val show = findViewById<View>(R.id.showSheet)
        val close = findViewById<View>(R.id.closeButton)
        show.setOnClickListener {
            mn.showBottomSheet(BottomSheetExample(),"Hello")
        }
        close.setOnClickListener {
            mn.hideBottomSheet()
        }

        sceneMn = AsynchronousSceneManager(
            Scene(
                NoOpDictator(), VSpotGuide(
                    close,
                    "Close Bottom Sheet", "Click button to hide bottom sheet"
                ), BottomSheetWatcher(behavior)
            ),
            Scene(
                NoOpDictator(), VSpotGuide(
                    show,
                    "Show Bottom Sheet", "Click button to reveal bottom sheet"
                ),
                NoOpWatcher()
            )
        )
        sceneMn.beginTour(1000)
    }
}