package com.ume.util

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class SoftInputWatcher(
    activity: Activity,
    private val visibilityCallback: InputVisibilityChangeListener
) : DefaultLifecycleObserver, OnApplyWindowInsetsListener, OnGlobalLayoutListener {

    private var navOffset = -1
    private val content: View = activity.findViewById(android.R.id.content)
    private val bounds: Rect = Rect()

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        ViewCompat.setOnApplyWindowInsetsListener(content, null)
        content.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        ViewCompat.setOnApplyWindowInsetsListener(content, this)
        content.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        possiblyResizeChildOfContent()
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        val navigationBars = WindowInsetsCompat.Type.navigationBars()
        if (insets.isVisible(navigationBars)) {
            val nav = insets.getInsets(navigationBars)
            navOffset = nav.bottom
        }

        return insets
    }

    private fun possiblyResizeChildOfContent() {
        content.getWindowVisibleDisplayFrame(bounds)
        val offset = content.bottom - bounds.bottom
        visibilityCallback.onInputVisibilityChanged(
            offset, offset > navOffset && navOffset != -1
        )
    }


    interface InputVisibilityChangeListener {
        /**
         * @param offset The amount of space used by soft input
         * @param visible whether input is visible or not. This value is a guessed value based on threshold
         */
        fun onInputVisibilityChanged(offset: Int, visible: Boolean)
    }
}