package com.ume.adapter.callback

import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder

interface AdapterItemListener {

    fun onAdapterItemClicked(holder: ViewHolder, v: View) {}

    fun onAdapterMenuClicked(holder: ViewHolder, item: Any) {}
}