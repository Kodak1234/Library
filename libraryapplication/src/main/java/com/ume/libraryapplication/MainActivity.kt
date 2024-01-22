package com.ume.libraryapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.ume.libraryapplication.screens.TrimFragment
import com.ume.navigation.navigation.NavigationControllerCallback
import com.ume.navigation.navigation.NavigationControllerImpl
import com.ume.navigation.navigation.NavigationControllerImpl.Info

class MainActivity : AppCompatActivity(), ScreenFragment.ScreenSelectionListener,
    NavigationControllerCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val controller = NavigationControllerImpl(
            Info(
                R.id.fraContainer, supportFragmentManager, this
            )
        )

        val nav: BottomNavigationView = findViewById(R.id.nav)
        nav.setOnItemSelectedListener {
            controller.select(it.itemId)
            true
        }
        controller.select(R.id.tab1)

    }

    override fun onScreenSelected(screen: ScreenFragment.Screen) {
        val frag = when (screen.id) {
            ScreenFragment.TRIM_SCREEN -> TrimFragment()
            else -> null
        }

        if (frag != null) {
            supportFragmentManager.beginTransaction().replace(R.id.fraContainer, frag)
                .addToBackStack(null).commit()
        }
    }

    override fun getFragmentTag(id: Int): String {
        return when (id) {
            R.id.tab1 -> {
                "tab1"
            }

            R.id.tab2 -> {
                "tab2"
            }

            else -> throw IllegalArgumentException()
        }
    }

    override fun getFragment(id: Int): Fragment {
        return when (id) {
            R.id.tab1 -> {
                FragmentSheet()
            }

            R.id.tab2 -> {
                BottomSheetExample()
            }

            else -> throw IllegalArgumentException()
        }
    }
}