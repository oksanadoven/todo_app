package com.example.totolist.tabs_fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.totolist.day_fragment.DayFragment
import com.example.totolist.details.TaskDetailsFragment
import com.example.totolist.month_fragment.MonthFragment

class ViewPagerFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MonthFragment()
            1 -> DayFragment()
            else -> TaskDetailsFragment()
        }
    }
}