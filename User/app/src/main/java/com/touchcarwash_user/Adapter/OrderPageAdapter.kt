package com.touchcarwash_user.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.touchcarwash_user.fragments.FinisedFragment
import com.touchcarwash_user.fragments.OnGoingFragment
import com.touchcarwash_user.fragments.PendingFragment

class OrderPageAdapter(
        fm: FragmentManager,
        count: Int
) : FragmentStatePagerAdapter(fm) {

    private var tabs = count
    lateinit var pendingOrders: PendingFragment
    lateinit var onGoingOrders: OnGoingFragment
    lateinit var finishedOrders: FinisedFragment


    override fun getItem(p0: Int): Fragment {
        when (p0) {
            0 -> {
                pendingOrders = PendingFragment()
                return pendingOrders
            }

            1 -> {
                onGoingOrders = OnGoingFragment()
                return onGoingOrders
            }

            2 -> {
                finishedOrders = FinisedFragment()
                return finishedOrders
            }

            else -> {
                pendingOrders = PendingFragment()
                return pendingOrders
            }
        }
    }

    override fun getCount(): Int {
        return tabs
    }

}