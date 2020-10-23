package com.example.totolist

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.totolist.calendar.CalendarFragment
import com.example.totolist.calendar_day.CalendarDailyFragment
import com.example.totolist.details.TaskDetailsFragment
import com.example.totolist.list_cardview.TaskListFragment
import com.example.totolist.utils.TaskListMode

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    CalendarDailyFragment()
                    //CalendarFragment()
                    //TaskListFragment()
                )
                .commit()
        }
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is CalendarFragment) {
            fragment.listener = object : CalendarFragment.Listener {
                override fun onActionAddSelected(id: Long, date: String?) {
                    openTaskScreen(id, date)
                }
            }
            val observer = Observer<TaskListMode> { mode ->
                val checkedItemsText = findViewById<TextView>(R.id.checked_items_text)
                val searchField = findViewById<EditText>(R.id.search_field)
                when (mode) {
                    is TaskListMode.Normal -> {
                        checkedItemsText.text = ""
                        searchField.isVisible = false
                        supportActionBar?.setDisplayShowTitleEnabled(true)
                    }
                    is TaskListMode.Search -> {
                        supportActionBar?.setDisplayShowTitleEnabled(false)
                        searchField.isVisible = true
                        searchField.setText("", TextView.BufferType.EDITABLE)
                        searchField.doOnTextChanged { text, _, _, _ ->
                            fragment.setQuery(text.toString())
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
                invalidateOptionsMenu()
            }
            fragment.mode.observe(this, observer)
        }
        if (fragment is CalendarDailyFragment) {
            fragment.onClickListener = object : CalendarDailyFragment.OnMenuClickListener {
                override fun onActionAddClicked(id: Long, date: String) {
                    openTaskScreen(id, date)
                }
            }
        }
        if (fragment is TaskListFragment) {
            fragment.listener = object : TaskListFragment.Listener {
                override fun onTaskSelected(id: Long, date: String?) {
                    openTaskScreen(id, date)
                }
            }
            val observer = Observer<TaskListMode> { mode ->
                val checkedItemsText = findViewById<TextView>(R.id.checked_items_text)
                val searchField = findViewById<EditText>(R.id.search_field)
                when (mode) {
                    is TaskListMode.Select -> {
                        supportActionBar?.setDisplayShowTitleEnabled(false)
                        checkedItemsText.text = "${mode.selectedItemsCount} selected"
                    }
                    is TaskListMode.Normal -> {
                        checkedItemsText.text = ""
                        searchField.isVisible = false
                        supportActionBar?.setDisplayShowTitleEnabled(true)
                    }
                    is TaskListMode.Search -> {
                        supportActionBar?.setDisplayShowTitleEnabled(false)
                        searchField.isVisible = true
                        searchField.setText("", TextView.BufferType.EDITABLE)
                        searchField.doOnTextChanged { text, _, _, _ ->
                            fragment.setQuery(text.toString())
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
                invalidateOptionsMenu()
            }
            fragment.mode.observe(this, observer)
        }
        if (fragment is TaskDetailsFragment) {
            fragment.saveItemListener = object : TaskDetailsFragment.SaveItemListener {
                override fun onItemSaved() {
                    supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun openTaskScreen(id: Long, date: String?) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                TaskDetailsFragment().apply {
                    arguments = Bundle().apply {
                        putLong(TaskDetailsFragment.ARG_TASK_ID, id)
                        putString(TaskDetailsFragment.ARG_TASK_DATE, date)
                    }
                })
            .addToBackStack(null)
            .commit()
    }

}