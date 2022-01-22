package com.ume.libraryapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ume.bottomsheet.BottomSheetManager
import com.ume.phone.PhoneUtil

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val show = findViewById<View>(R.id.showSheet)
        show.setOnClickListener {
            BottomSheetManager.find(this)!!.showBottomSheet(BottomSheetExample())
        }

        val v = findViewById<TextView>(R.id.text)
        v.text = PhoneUtil.create(this)
            .getCode()

        val sheet: View = findViewById(R.id.container)
        BottomSheetManager.attach(
            this, null,
            findViewById(R.id.container),
            BottomSheetBehavior.from(sheet)
        )
    }
}