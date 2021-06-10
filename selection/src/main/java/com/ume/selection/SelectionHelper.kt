package com.ume.selection

import android.os.Bundle
import android.util.SparseIntArray
import androidx.recyclerview.widget.RecyclerView

class SelectionHelper(private val adapter: RecyclerView.Adapter<*>, private val multiple: Boolean) {
    private val SELECTED_POSITION = "ume.itemCheckHelper.position"
    var pos = -1

    /**
     * Values in the structure is offset by +1. To get actual adapter position
     * offset the value by -1.
     *
     * @return SparseIntArray of selected positions or null if in single select mode
     */
    var selectedPositions: SparseIntArray? = null
        private set

    init {
        if (multiple)
            selectedPositions = SparseIntArray()
    }

    fun addPos(pos: Int) {
        selectedPositions!!.put(pos, pos + 1)
    }

    fun save(state: Bundle) {
        if (multiple)
            state.putIntArray(SELECTED_POSITION, toInt())
        else
            state.putInt(SELECTED_POSITION, pos)
    }

    fun restore(state: Bundle?) {
        if (state == null) return
        if (multiple)
            fromInt(state.getIntArray(SELECTED_POSITION)!!)
        else
            pos = state.getInt(SELECTED_POSITION, -1)
    }

    /**
     * Call when a new item is added
     */
    fun itemAddedAt(p: Int) {
        if (multiple) {
            val len = selectedPositions!!.size()
            //increment affected positions by 1
            for (i in len - 1 downTo 0) {
                val pos = selectedPositions!!.keyAt(i)
                if (pos >= p) {
                    selectedPositions!!.removeAt(i)
                    selectedPositions!!.put(pos + 1, pos + 2)
                } else
                    break
            }
        } else if (pos >= p)
            pos += 1
    }

    fun itemRemovedAt(p: Int) {
        if (multiple) {
            var len = selectedPositions!!.size()
            //decrement affected positions by 1
            var i = 0
            while (i < len) {
                val pos = selectedPositions!!.keyAt(i)
                if (pos >= p) {
                    selectedPositions!!.removeAt(i)
                    if (pos == p) {
                        i -= 1
                        len -= 1
                    } else
                        selectedPositions!!.put(pos - 1, pos)
                }
                i++
            }
        } else {
            if (pos == p)
                pos = -1
            else if (pos > p)
                pos -= 1
        }
    }

    /**
     * Call from Adapter.onBindViewHolder
     */
    fun checkItem(check: Boolean.() -> Unit, p: Int) {
        check(multiple && selectedPositions!!.get(p) > 0 || p == pos)
    }

    /**
     * Call to select an item
     */
    fun select(position: Int, shouldNotify: Boolean = true) {
        if (multiple)
            multipleSelect(position, shouldNotify)
        else
            singleSelect(position, shouldNotify)
    }

    private fun multipleSelect(position: Int, shouldNotify: Boolean) {
        val p = selectedPositions!!.get(position)
        //has not been selected b4
        //add to selectedPositions
        if (p == 0) {
            pos = position
            selectedPositions!!.put(position, position + 1)
            if (shouldNotify)
                adapter.notifyItemChanged(position)
        } else {
            //re-select an item means a deselect, so remove it from selectedPositions
            pos = -1
            selectedPositions!!.delete(position)
            if (shouldNotify)
                adapter.notifyItemChanged(position)
        }

    }

    private fun singleSelect(position: Int, shouldNotify: Boolean) {
        val p = pos
        pos = if (pos == position) -1 else position
        if (shouldNotify) {
            //check item
            if (pos != -1)
                adapter.notifyItemChanged(pos)
            //un-check item
            if (p != -1)
                adapter.notifyItemChanged(p)
        }

    }

    private fun toInt(): IntArray {
        val len = selectedPositions!!.size()
        val sel = IntArray(len)
        for (i in 0 until len) {
            sel[i] = selectedPositions!!.keyAt(i)
        }
        return sel
    }

    private fun fromInt(sel: IntArray) {
        for (i in sel) {
            addPos(i)
        }
    }
}