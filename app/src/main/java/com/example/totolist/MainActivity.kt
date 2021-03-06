package com.example.totolist

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.totolist.details.TaskDetailsFragment
import com.example.totolist.goups.GroupsFragment
import com.example.totolist.search_fragment.SearchFragment
import com.example.totolist.tabs_fragment.TabsFragment
import com.example.totolist.utils.TaskListMode
import com.jakewharton.threetenabp.AndroidThreeTen

class MainActivity : AppCompatActivity() {

    companion object {
        private const val SEARCH_FRAGMENT_TAG = "SEARCH_FRAGMENT_TAG"
        private const val DETAILS_FRAGMENT_TAG = "DETAILS_FRAGMENT_TAG"
    }

    interface ActionBarListener {
        fun closeSearchModeRequested(fragment: Fragment)
    }

    private lateinit var searchField: EditText
    private var isSearchModeEnabled: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    private val mode: LiveData<TaskListMode> = MediatorLiveData<TaskListMode>().apply {
        value = TaskListMode.Normal
        addSource(isSearchModeEnabled) {
            value = if (isSearchModeEnabled.value!!) {
                TaskListMode.Search
            } else {
                TaskListMode.Normal
            }
        }
    }
    private var actionBarListener: ActionBarListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TabsFragment())
                .commit()
        }
        setUpSearchField()
        val observer = Observer<TaskListMode> { mode ->
            when (mode) {
                is TaskListMode.Normal -> {
                    removeSearchFragmentIfExists()
                }
                is TaskListMode.Search -> {
                    addSearchFragmentIfRequired()
                    searchField.setText("")
                }
            }
            invalidateOptionsMenu()
        }
        mode.observe(this, observer)
    }

    private fun findSearchFragment(): SearchFragment? {
        return supportFragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG) as? SearchFragment
    }

    private fun addSearchFragmentIfRequired() {
        if (findSearchFragment() != null) {
            return
        }
        val searchFragment = SearchFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, searchFragment, SEARCH_FRAGMENT_TAG)
            .commitNow()
    }

    private fun removeSearchFragmentIfExists() {
        val fragment = findSearchFragment() ?: return
        supportFragmentManager.beginTransaction()
            .remove(fragment)
            .commit()
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is TabsFragment) {
            fragment.tabFragmentClickListener = object : TabsFragment.OnAddListClickListener {
                override fun onAddButtonClicked(id: Long, date: Long) {
                    openTaskScreen(id, date)
                }
            }
            fragment.searchScreenRequested = object : TabsFragment.SearchScreenRequested {
                override fun openSearchScreenRequested() {
                    supportFragmentManager.beginTransaction()
                        .add(
                            R.id.fragment_container,
                            SearchFragment()
                        )
                        .commit()
                }
            }
        }
        if (fragment is SearchFragment) {
            fragment.openDetailsListener = object : SearchFragment.OpenDetailsListener {
                override fun openDetailsScreenRequested(id: Long, date: Long) {
                    openTaskScreen(id, date)
                }
            }
            fragment.listener = object : SearchFragment.SearchDiscardListener {
                override fun searchDiscardRequested() {
                    actionBarListener?.closeSearchModeRequested(fragment)
                }
            }
            invalidateOptionsMenu()
        }
        if (fragment is TaskDetailsFragment) {
            fragment.addGroupListener = object : TaskDetailsFragment.AddGroupListener {
                override fun onAddGroupForTask(groupId: Long, taskId: Long) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, GroupsFragment().apply {
                            arguments = Bundle().apply {
                                putLong(GroupsFragment.GROUP_ID, groupId)
                                putLong(GroupsFragment.TASK_ID, taskId)
                            }
                        })
                        .addToBackStack("")
                        .commit()
                }
            }
            fragment.saveItemListener = object : TaskDetailsFragment.SaveItemListener {
                override fun onItemSaved() {
                    supportFragmentManager.popBackStack()
                }
            }
            fragment.listener = object : TaskDetailsFragment.TaskDeleteListener {
                override fun onTaskDeleted() {
                    supportFragmentManager.popBackStack()
                }
            }
            invalidateOptionsMenu()
        }
        if (fragment is GroupsFragment) {
            actionBar?.setDisplayHomeAsUpEnabled(true)
            fragment.onGroupChosenListener = object : GroupsFragment.SetGroupForTaskListener {
                override fun onGroupSelected() {
                    supportFragmentManager.popBackStack()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setUpSearchField() {
        searchField = findViewById(R.id.search_field)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        searchField.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val inputMethodManager =
                    this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
        searchField.doOnTextChanged { text, _, _, _ ->
            findSearchFragment()?.setQuery(text.toString())
        }
    }

    private fun openTaskScreen(id: Long, date: Long) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                TaskDetailsFragment().apply {
                    arguments = Bundle().apply {
                        putLong(TaskDetailsFragment.ARG_TASK_ID, id)
                        putLong(TaskDetailsFragment.ARG_TASK_DATE, date)
                    }
                }, DETAILS_FRAGMENT_TAG
            )
            .addToBackStack(null)
            .commit()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isSearchModeOn = mode.value is TaskListMode.Search
        menu.findItem(R.id.action_search).isVisible =
            (!isSearchModeOn && this.supportFragmentManager.findFragmentByTag(DETAILS_FRAGMENT_TAG) == null)
        menu.findItem(R.id.action_add_new_task).isVisible =
            (!isSearchModeOn && this.supportFragmentManager.findFragmentByTag(DETAILS_FRAGMENT_TAG) == null)
        searchField.isVisible = isSearchModeOn
        menu.findItem(R.id.action_clear_search).isVisible = isSearchModeOn
        prepareMenuForDetailsFragment(isSearchModeOn, menu)
        supportActionBar?.setDisplayShowTitleEnabled(!isSearchModeOn)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                if (isSearchModeEnabled.value == false) {
                    isSearchModeEnabled.value = true
                }
                true
            }
            R.id.action_clear_search -> {
                if (mode.value is TaskListMode.Search) {
                    isSearchModeEnabled.value = false
                }
                true
            }
            else -> false
        }
    }

    private fun prepareMenuForDetailsFragment(isSearchModeOn: Boolean, menu: Menu) {
        if (isSearchModeOn && this.supportFragmentManager.findFragmentByTag(DETAILS_FRAGMENT_TAG) != null) {
            menu.findItem(R.id.action_add_new_task).isVisible = false
            searchField.isVisible = false
            menu.findItem(R.id.action_clear_search).isVisible = false
            supportActionBar?.setDisplayShowTitleEnabled(true)
        }
        menu.findItem(R.id.action_delete).isVisible =
            (this.supportFragmentManager.findFragmentByTag(DETAILS_FRAGMENT_TAG) != null)
    }

}