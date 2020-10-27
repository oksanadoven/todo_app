package com.example.totolist

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.totolist.calendar.CalendarFragment
import com.example.totolist.calendar_day.CalendarDailyFragment
import com.example.totolist.list_cardview.TaskListFragment

class ViewPagerFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CalendarFragment()
            1 -> CalendarDailyFragment()
            else -> TaskListFragment()
        }
    }
}