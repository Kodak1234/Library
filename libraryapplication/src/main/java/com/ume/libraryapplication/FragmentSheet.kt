package com.ume.libraryapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ume.bottomsheet.BottomSheetManager
import com.ume.bottomsheet.SheetConfig
import com.ume.libraryapplication.screens.DemoFragment
import com.ume.util.dp

class FragmentSheet : Fragment() {

    private lateinit var bottomSheetState: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bottomSheetState = savedInstanceState?.getBundle("Sheet") ?: Bundle()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle("Sheet", bottomSheetState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sheet = view.findViewById<ViewGroup>(R.id.container)
        val show = view.findViewById<View>(R.id.showSheet)
        val close = view.findViewById<View>(R.id.closeButton)

        val manager = BottomSheetManager.Builder(sheet)
            .setFragmentManager(childFragmentManager)
            .setState(bottomSheetState)
            .setOwner(viewLifecycleOwner)
            .build()

        show.setOnClickListener {
            val overlay = childFragmentManager.findFragmentById(R.id.container) != null
            manager.showBottomSheet(
                DemoFragment(), SheetConfig()
                    .setCancelable(overlay)
                    .setDimColor(Color.RED)
                    .setElevation(resources.dp(8f))
                    .setOverlay(overlay)
                    .setPeekHeight(if (overlay) resources.dp(200) else 0)
                    .setAnimateRadius(true)
                    .setCornerRadius(16f)
            )
        }
        close.setOnClickListener {
            manager.closeBottomSheet()
        }
    }
}