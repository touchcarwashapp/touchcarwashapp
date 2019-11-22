package com.touchcarwash_user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.touchcarwash_user.Adapter.OrderPageAdapter
import kotlinx.android.synthetic.main.activity_booking_tabs.*
import kotlinx.android.synthetic.main.activity_wash_vehicles.*

class BookingTabs : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_tabs)

        //setting up tabs and order page adapter
        val orderPageAdapter = OrderPageAdapter(supportFragmentManager, tabLayout.tabCount)
        tabViewPager.adapter = orderPageAdapter

        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabReselected(p0: TabLayout.Tab?) {
                //
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                //
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                tabViewPager.currentItem = p0!!.position
            }

        })

        tabViewPager.addOnPageChangeListener(object: TabLayout.TabLayoutOnPageChangeListener(tabLayout){})

    }
}