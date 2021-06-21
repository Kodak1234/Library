package com.ume.libraryapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.ume.phone.PhoneUtil

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val v = findViewById<TextView>(R.id.text)
        v.text = PhoneUtil.create(this)
            .getCode()
    }
}