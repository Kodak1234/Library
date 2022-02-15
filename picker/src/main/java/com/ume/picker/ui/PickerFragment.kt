package com.ume.picker.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ume.adapter.DelegateAdapter
import com.ume.adapter.callback.AdapterItemListener
import com.ume.picker.R
import com.ume.picker.data.MediaDataSource
import com.ume.picker.data.MediaItem
import com.ume.selection.SelectionHelper
import com.ume.util.OffsetItemDecoration
import com.ume.util.dp
import kotlinx.parcelize.Parcelize
import kotlin.math.max;

class PickerFragment : Fragment(R.layout.fragment_picker), AdapterItemListener {

    private lateinit var adapter: DelegateAdapter
    private lateinit var source: MediaDataSource
    private var config: Config? = null
    private val selector by lazy { SelectionHelper(adapter, true) }
    private val model by lazy {
        ViewModelProvider(
            this,
            MediaDataViewModel.Factory(
                requireActivity().application,
                config!!.types
            )
        )[MediaDataViewModel::class.java]
    }

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
        val a = context.obtainStyledAttributes(attrs, R.styleable.Picker)
        val span = a.getInt(R.styleable.Picker_picker_spanCount, -1)
        val orientation = a.getInt(R.styleable.Picker_picker_orientation, RecyclerView.VERTICAL)
        val types = a.getInt(R.styleable.Picker_picker_types, 0)
        config = Config(span, orientation, types)
        a.recycle()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = config ?: savedInstanceState?.getParcelable(CONFIG)
        config = config ?: arguments?.getParcelable(CONFIG)
        adapter = DelegateAdapter(requireContext())
        adapter.addDelegate(MediaDelegate(selector, this))
        source = MediaDataSource(adapter)
        adapter.source = source
        selector.restore(savedInstanceState)

        if (checkSelfPermission(requireContext(), READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            model.load()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = view.findViewById<RecyclerView>(R.id.list)
        list.adapter = adapter
        val mn = GridLayoutManager(
            requireContext(), max(1, config!!.spanCount),
            config!!.orientation, false
        )
        list.layoutManager = mn
        model.cursor.observe(viewLifecycleOwner) {
            selector.selectedPositions?.clear()
            source.cursor = it
        }

        if (config!!.spanCount == -1) {
            list.doOnPreDraw {
                mn.spanCount = (list.width / resources.dp(100)) + 1
            }
        }

        val offset = resources.dp(1)
        list.addItemDecoration(OffsetItemDecoration(offset, offset, offset, offset))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selector.save(outState)
        outState.putParcelable(CONFIG, config)
    }

    companion object {
        const val CONFIG = "com.ume.picker.config"
    }

    @Parcelize
    class Config(var spanCount: Int, val orientation: Int, val types: Int) : Parcelable

    interface Callback {
        /**
         * Called when user selects a media
         * @param media media that was selected
         * @return true to select the item, false to unselect
         */
        fun onMediaSelected(media: MediaItem): Boolean

        /**
         * Called when user unselects a media
         * @param media media that was unselected
         */
        fun onMediaUnSelected(media: MediaItem)

        /**
         * Called when media size changes
         * @param size number of media loaded from system
         */
        fun onMediaSizeChanged(size: Int)
    }
}