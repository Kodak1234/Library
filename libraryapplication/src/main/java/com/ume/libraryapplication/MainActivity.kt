package com.ume.libraryapplication

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.guidedtour.Scene
import com.example.guidedtour.SceneManager
import com.example.guidedtour.impl.DummyDictator
import com.example.guidedtour.impl.DummyWatcher
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ume.bottomsheet.BottomSheetManager
import com.ume.phone.PhoneUtil

class MainActivity : AppCompatActivity() {
    private lateinit var sceneMn: SceneManager
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
            mn.showBottomSheet(BottomSheetExample())
        }
        close.setOnClickListener {
            mn.hideBottomSheet()
        }

        sceneMn = SceneManager(
            Scene(
                DummyDictator(), VSpotGuide(
                    show,
                    "Show Bottom Sheet", "Click button to reveal bottom sheet"
                ),
                DummyWatcher()
            ), Scene(
                DummyDictator(), VSpotGuide(
                    close,
                    "Close Bottom Sheet", "Click button to hide bottom sheet"
                ), BottomSheetWatcher(behavior)
            )
        )
        sceneMn.nextTour()
    }
}