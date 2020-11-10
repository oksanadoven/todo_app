package com.example.totolist.tabs_fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.totolist.R
import com.example.totolist.day_fragment.DayFragment
import com.example.totolist.month_fragment.MonthFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TabsFragment : Fragment(R.layout.fragment_tabs) {

    interface OnAddListClickListener {
        fun onAddButtonClicked(id: Long, date: Long)
    }

    interface SearchScreenRequested {
        fun openSearchScreenRequested()
    }

    private val tabTitles = arrayOf("Month", "Day")
    private lateinit var viewPager: ViewPager2
    var tabFragmentClickListener: OnAddListClickListener? = null
    var searchScreenRequested: SearchScreenRequested? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Instantiate a ViewPager2 and a PagerAdapter
        viewPager = view.findViewById(R.id.view_pager)
        viewPager.isUserInputEnabled = false
        val pagerFragmentAdapter = ViewPagerFragmentAdapter(childFragmentManager, this.lifecycle)
        viewPager.adapter = pagerFragmentAdapter
        // Attaching tab mediator
        val tabs = view.findViewById<TabLayout>(R.id.tabs)
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = tabTitles[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        if (childFragment is MonthFragment) {
            childFragment.listener = object : MonthFragment.Listener {
                override fun onActionAddSelected(id: Long, date: Long) {
                    tabFragmentClickListener?.onAddButtonClicked(id, date)
                }
            }
            childFragment.searchListener = object : MonthFragment.SearchScreenListener {
                override fun searchActionSelected() {
                    searchScreenRequested?.openSearchScreenRequested()
                }
            }
        }
        if (childFragment is DayFragment) {
            childFragment.onClickListener = object : DayFragment.OnMenuClickListener {
                override fun onActionAddClicked(id: Long, date: Long) {
                    tabFragmentClickListener?.onAddButtonClicked(id, date)
                }
            }
        }
    }
}