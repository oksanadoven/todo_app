package com.example.totolist

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.example.totolist.calendar.CalendarFragment
import com.example.totolist.calendar_day.CalendarDailyFragment
import com.example.totolist.utils.TaskListMode
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TabsFragment : Fragment(R.layout.fragment_tabs) {

    interface OnAddListClickListener {
        fun onAddButtonClicked(id: Long, date: String)
    }

    private val tabTitles = arrayOf("Month", "Day")
    private lateinit var viewPager: ViewPager2
    var tabFragmentClickListener: OnAddListClickListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Instantiate a ViewPager2 and a PagerAdapter
        viewPager = view.findViewById(R.id.view_pager)
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
        if (childFragment is CalendarFragment) {
            childFragment.listener = object : CalendarFragment.Listener {
                override fun onActionAddSelected(id: Long, date: String?) {
                    tabFragmentClickListener?.onAddButtonClicked(id, date!!)
                }
            }
            val observer = Observer<TaskListMode> { mode ->
                val checkedItemsText = view?.findViewById<TextView>(R.id.checked_items_text)
                val searchField = view?.findViewById<EditText>(R.id.search_field)
                /*when (mode) {
                    is TaskListMode.Normal -> {
                        checkedItemsText!!.text = ""
                        searchField!!.isVisible = false
                        supportActionBar.setDisplayShowTitleEnabled(true)
                    }
                    is TaskListMode.Search -> {
                        supportActionBar.setDisplayShowTitleEnabled(false)
                        searchField!!.isVisible = true
                        searchField.setText("", TextView.BufferType.EDITABLE)
                        searchField.doOnTextChanged { text, _, _, _ ->
                            childFragment.setQuery(text.toString())
                        }
                        searchField.setOnFocusChangeListener { v, hasFocus ->
                            if (v.id == R.id.search_field && !hasFocus) {
                                val inputMethodManager: InputMethodManager =
                                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
                            }
                        }
                    }
                }
                invalidateOptionsMenu()*/
            }
            childFragment.mode.observe(this, observer)
        }
        if (childFragment is CalendarDailyFragment) {
            childFragment.onClickListener = object : CalendarDailyFragment.OnMenuClickListener {
                override fun onActionAddClicked(id: Long, date: String) {
                    tabFragmentClickListener?.onAddButtonClicked(id, date)
                }
            }
        }
    }
}