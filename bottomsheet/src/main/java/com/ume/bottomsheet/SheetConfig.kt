package com.ume.bottomsheet

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable

open class SheetConfig() : Parcelable {
    var tag: String? = null
        private set

    var cancelable: Boolean = true
        private set

    var elevation: Float = 0f
        private set

    var cornerRadius: Float = 0f
        private set

    var opacity: Float = 0.5f
        private set

    var dimColor: Int = Color.BLACK
        private set

    var animateRadius: Boolean = true
        private set

    var allowClickBehind: Boolean = true
        private set

    var overlay: Boolean = false
        private set

    var peekHeight: Int = 0
        private set

    constructor(parcel: Parcel) : this() {
        cancelable = parcel.readInt() == 1
        elevation = parcel.readFloat()
        cornerRadius = parcel.readFloat()
        opacity = parcel.readFloat()
        dimColor = parcel.readInt()
        animateRadius = parcel.readInt() == 1
        allowClickBehind = parcel.readInt() == 1
        overlay = parcel.readInt() == 1
        peekHeight = parcel.readInt()
    }

    fun setTag(tag: String): SheetConfig {
        this.tag = tag
        return this
    }

    fun setCancelable(cancelable: Boolean): SheetConfig {
        this.cancelable = cancelable
        return this
    }

    fun setElevation(elevation: Float): SheetConfig {
        this.elevation = elevation
        return this
    }

    fun setCornerRadius(radius: Float): SheetConfig {
        this.cornerRadius = radius
        return this
    }

    fun setOpacity(opacity: Float): SheetConfig {
        this.opacity = opacity
        return this
    }

    fun setDimColor(color: Int): SheetConfig {
        this.dimColor = color
        return this
    }

    fun setAnimateRadius(animate: Boolean): SheetConfig {
        this.animateRadius = animate
        return this
    }

    fun setAllowClickBehind(allow: Boolean): SheetConfig {
        allowClickBehind = allow
        return this
    }

    fun setOverlay(overlay: Boolean): SheetConfig {
        this.overlay = overlay
        return this
    }

    fun setPeekHeight(height: Int): SheetConfig {
        peekHeight = height
        return this
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(if (cancelable) 1 else 0)
        parcel.writeFloat(elevation)
        parcel.writeFloat(cornerRadius)
        parcel.writeFloat(opacity)
        parcel.writeInt(dimColor)
        parcel.writeInt(if (animateRadius) 1 else 0)
        parcel.writeInt(if (allowClickBehind) 1 else 0)
        parcel.writeInt(if (overlay) 1 else 0)
        parcel.writeInt(peekHeight)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SheetConfig> {
        override fun createFromParcel(parcel: Parcel): SheetConfig {
            return SheetConfig(parcel)
        }

        override fun newArray(size: Int): Array<SheetConfig?> {
            return arrayOfNulls(size)
        }
    }
}