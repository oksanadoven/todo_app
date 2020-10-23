package com.example.totolist.list_for_date

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R
import com.example.totolist.TaskListItem
import com.example.totolist.calendar.*
import com.example.totolist.database.TasksDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskListForDateFragment : Fragment() {

    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var recyclerView: RecyclerView
    private val calendarListAdapter: CalendarListAdapter = CalendarListAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = requireNotNull(this.activity).application
        val dataSource = TasksDatabase.getInstance(application).tasksDatabaseDao
        val viewModelFactory = CalendarViewModelFactory(dataSource, application)
        calendarViewModel =
            ViewModelProvider(this, viewModelFactory).get(CalendarViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar_task_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerView.adapter = calendarListAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        syncCalendarItemsWithDate()
        calendarListAdapter.listener = object : CalendarListAdapter.OnItemChecked {
            override fun onItemChecked(itemId: Long, isDone: Boolean) {
                lifecycleScope.launch {
                    calendarViewModel.updateTaskItems(itemId, isDone)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.calendar_recycler_view)
    }

    private fun syncCalendarItemsWithDate() {
        val date = arguments?.getString(ARG_TASK_DATE)
        if (date != null) {
            lifecycleScope.launch {
                val items = calendarViewModel.getCalendarItemsByDate(date)
                withContext(Dispatchers.Main) {
                    calendarListAdapter.submitList(items)
                }
            }
        }
    }

    fun setSearchList(taskList: List<TaskListItem>) {
        val searchResultList = taskList.flatMap { taskListItem ->
            val items = ArrayList<CalendarListItem>()
            items.add(CalendarTaskHeaderItem(taskListItem.taskWithItems.task))
            val taskItems = taskListItem.taskWithItems.items.map { taskItem ->
                CalendarTaskCheckboxItem(taskItem)
            }
            items.addAll(taskItems)
            items
        }
        calendarListAdapter.submitList(searchResultList)
    }


    companion object {
        const val ARG_TASK_DATE = "ARG_TASK_DATE"
    }
}