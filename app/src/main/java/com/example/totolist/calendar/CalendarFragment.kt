package com.example.totolist.calendar

import android.os.Bundle
import android.view.*
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.totolist.R
import com.example.totolist.database.TasksDatabase
import com.example.totolist.list_for_date.TaskListForDateFragment
import com.example.totolist.utils.TaskListMode
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    interface Listener {
        fun onActionAddSelected(id: Long, date: String?)
    }

    private lateinit var viewModel: CalendarViewModel
    private lateinit var calendar: CalendarView
    private lateinit var currentDate: TextView
    private val calendarInstance = Calendar.getInstance()
    var listener: Listener? = null

    //private var taskListForDateFragment = TaskListForDateFragment()
    private var isSearchModeEnabled: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    val mode: LiveData<TaskListMode> = MediatorLiveData<TaskListMode>().apply {
        value = TaskListMode.Normal
        addSource(isSearchModeEnabled) {
            value = if (isSearchModeEnabled.value!!) {
                TaskListMode.Search
            } else {
                TaskListMode.Normal
            }
        }
    }
    private val date = MutableLiveData<String>().apply {
        value = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(calendarInstance.time)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        //Instance of database
        val application = requireNotNull(this.activity).application
        val dataSource = TasksDatabase.getInstance(application).tasksDatabaseDao
        //Instance of the ViewModel Factory
        val viewModelFactory = CalendarViewModelFactory(dataSource, application)
        //Instance of the ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory).get(CalendarViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_calendar_fagment, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        openFragmentForDate()
        calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendarInstance.set(year, month, dayOfMonth)
            val dateFormatter = SimpleDateFormat("EEEE d, MMM", Locale.ROOT)
            val formattedDate = dateFormatter.format(calendarInstance.time)
            date.value = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(calendarInstance.time)
            currentDate.text = formattedDate
            openFragmentForDate()
        }
        viewModel.taskItemsLiveData.observe(this, { taskList ->
            if (mode.value is TaskListMode.Search) {
                activity!!.supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.calendar_fragment_container,
                        TaskListForDateFragment().apply { setSearchList(taskList) })
                    .commit()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calendar = view.findViewById(R.id.calendar_view)
        currentDate = view.findViewById(R.id.current_date)
        calendar.setDate(System.currentTimeMillis(), false, true)
        currentDate.text = SimpleDateFormat("EEEE d, MMM", Locale.ROOT).format(calendar.date)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val showDiscardIcon = mode.value is TaskListMode.Search
        menu.findItem(R.id.action_discard_selection).isVisible = showDiscardIcon
        menu.findItem(R.id.icon_action_search).isVisible = !showDiscardIcon
        menu.findItem(R.id.icon_action_add).isVisible = mode.value is TaskListMode.Normal
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.icon_action_add -> {
                listener?.onActionAddSelected(0L, date.value)
                true
            }
            R.id.icon_action_search -> {
                if (isSearchModeEnabled.value == false) {
                    isSearchModeEnabled.value = true
                }
                true
            }
            R.id.action_discard_selection -> {
                if (mode.value is TaskListMode.Search) {
                    isSearchModeEnabled.value = false
                    setQuery("")
                }
                true
            }
            else -> false
        }
    }

    private fun openFragmentForDate() {
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.calendar_fragment_container, TaskListForDateFragment().apply {
                arguments = Bundle().apply {
                    putString(TaskListForDateFragment.ARG_TASK_DATE, date.value)
                }
            })
            .commit()
    }

    fun setQuery(text: String) {
        viewModel.setQuery(text)
    }
}