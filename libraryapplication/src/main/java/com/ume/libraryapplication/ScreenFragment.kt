package com.ume.libraryapplication

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.ListFragment

class ScreenFragment : ListFragment() {

    private lateinit var adapter: ArrayAdapter<Screen>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ArrayAdapter<Screen>(
            requireContext(), android.R.layout.simple_list_item_1,
            android.R.id.text1
        ).apply {
            add(Screen("Trim View", TRIM_SCREEN))
        }

        listAdapter = adapter
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        (requireContext() as ScreenSelectionListener)
            .onScreenSelected(adapter.getItem(position)!!)
    }


    class Screen(val name: String, val id: Int) {
        override fun toString(): String = name
    }

    interface ScreenSelectionListener {
        fun onScreenSelected(screen: Screen)
    }

    companion object {
        const val TRIM_SCREEN = 0
    }
}