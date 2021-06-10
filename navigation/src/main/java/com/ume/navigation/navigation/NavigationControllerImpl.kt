package com.ume.navigation.navigation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import java.util.*
import kotlin.collections.ArrayList


/**
 * Helper for managing fragments in BottomNavigationView.
 */
class NavigationControllerImpl(private val info: Info) : NavigationController {
    private val stack: LinkedList<Int> = LinkedList()
    private var currentId = info.state?.getInt(ID) ?: info.id

    init {
        info.state?.run {
            getIntegerArrayList(STACK)?.run {
                stack.addAll(this)
            }
        }
    }

    @Override
    override fun select(id: Int, run: () -> Unit) {
        currentId = id
        val tag = info.callback.getFragmentTag(id)
        val tran = attach(tag, run)
        if (tran != null) {
            tran.add(info.containerId, info.callback.getFragment(id), tag)
            tran.commit()
        }
    }

    override fun getId(): Int = currentId

    @Override
    override fun push(id: Int) {
        if (currentId != id) {
            stack.push(currentId)
            select(id)
        }
    }

    @Override
    override fun pop() {
        if (!stack.isEmpty()) {
            val id = currentId
            select(stack.pop()) {
                val tag = info.callback.getFragmentTag(id)
                findFragment(tag)?.run {
                    info.manager.beginTransaction()
                        .remove(this)
                        .commit()
                }
            }
        }
    }

    fun onSave(state: Bundle) {
        state.putInt(ID, currentId)
        if (!stack.isEmpty()) {
            state.putIntegerArrayList(STACK, ArrayList(stack))
        }
    }

    private fun getCurrentFragment(): Fragment? {
        return info.manager.findFragmentById(info.containerId)
    }

    private fun findFragment(tag: String): Fragment? {
        return info.manager.findFragmentByTag(tag)
    }

    /**
     * Tries to re-attach a detached fragment
     *
     * @param tag fragment tag
     * @return FragmentTransaction if the fragment was , false otherwise
     */
    private fun attach(tag: String, run: () -> Unit): FragmentTransaction? {
        val current = getCurrentFragment()

        val old = findFragment(tag)

        val transaction = info.manager.beginTransaction()
            .runOnCommit(run)
            .setCustomAnimations(info.callback.getAnimation(tag, true),
                info.callback.getAnimation(tag, false)
            )
        if (old != null) {
            if (current == old) {
                if (current.isDetached) {
                    transaction.attach(current).commit()
                } else {
                    Handler(Looper.getMainLooper()).post(run)
                }
                if (current is Scrollable)
                    current.scrollToTop()
                return null
            }

            //detach current frag and re-attach old
            current?.run {
                transaction.detach(this)
            }
            transaction.attach(old)

            transaction.commit()
            return null

        } else {
            if (current != null) {
                transaction.detach(current)
            }
            return transaction
        }
    }

    class Info(
        val containerId: Int, val manager: FragmentManager,
        val callback: NavigationControllerCallback,
        val state: Bundle? = null,
        val id: Int = -1
    )

    companion object {
        const val STACK = "NavigationControllerImpl.state:STACK"
        const val ID = "NavigationControllerImpl.state:ID"
    }
}
