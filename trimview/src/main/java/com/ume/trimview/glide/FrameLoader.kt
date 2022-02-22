package com.ume.trimview.glide

import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.signature.ObjectKey
import com.ume.trimview.data.Frame
import java.io.InputStream

class FrameLoader(private val context: Context) :
    ModelLoader<Frame, InputStream> {
    override fun buildLoadData(
        frame: Frame, width: Int, height: Int,
        options: Options
    ): LoadData<InputStream> {
        return LoadData(ObjectKey(frame), FrameFetcher(frame, context))
    }

    override fun handles(model: Frame): Boolean {
        return true
    }
}