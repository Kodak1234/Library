package com.ume.picker.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ume.adapter.DelegateAdapter
import com.ume.picker.R
import com.ume.picker.data.MediaDataSource
import com.ume.util.OffsetItemDecoration
import com.ume.util.dp

class PickerFragment : Fragment(R.layout.fragment_picker) {

    private lateinit var adapter: DelegateAdapter
    private lateinit var source: MediaDataSource
    private lateinit var perm: ActivityResultLauncher<String>
    private val model by lazy {
        ViewModelProvider(this)[MediaDataViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = DelegateAdapter(requireContext())
        adapter.addDelegate(ImageDelegate())
        source = MediaDataSource(adapter)
        adapter.source = source

        perm = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            PermissionCallback()
        )
        if (hasPermission())
            model.load()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = view.findViewById<RecyclerView>(R.id.list)
        list.adapter = adapter
        model.cursor.observe(viewLifecycleOwner) {
            source.cursor = it
        }

        val offset = resources.dp(1)
        list.addItemDecoration(OffsetItemDecoration(offset, offset, offset, offset))
    }

    override fun onResume() {
        super.onResume()
        if (!hasPermission()) perm.launch(READ_EXTERNAL_STORAGE)
    }

    private fun hasPermission() =
        checkSelfPermission(
            requireContext(),
            READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    inner class PermissionCallback : ActivityResultCallback<Boolean> {
        override fun onActivityResult(result: Boolean?) {
            if (result == true)
                model.load()
        }
    }
}