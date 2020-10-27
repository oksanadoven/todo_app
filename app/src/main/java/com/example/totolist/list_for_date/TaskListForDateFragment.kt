package com.example.totolist.list_for_date

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
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

    interface OnClickListener {
        fun onAddButtonClicked(id: Long, date: String)
    }

    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var recyclerView: RecyclerView
    private val calendarListAdapter: CalendarListAdapter = CalendarListAdapter()
    private lateinit var image: ImageView
    private lateinit var text: TextView
    private lateinit var addButton: Button
    private lateinit var emptyListVew: ConstraintLayout
    var listener: OnClickListener? = null

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
        emptyListVew = view.findViewById(R.id.empty_list)
    }

    private fun syncCalendarItemsWithDate() {
        val date = arguments?.getString(ARG_TASK_DATE)
        if (date != null) {
            lifecycleScope.launch {
                val items = calendarViewModel.getCalendarItemsByDate(date)
                withContext(Dispatchers.Main) {
                    calendarListAdapter.submitList(items)
                }
                if (items.isEmpty()) {
                    emptyListVew.isVisible = true
                    recyclerView.isVisible = false
                    image = view?.findViewById(R.id.image)!!
                    text = view?.findViewById(R.id.text)!!
                    addButton = view?.findViewById(R.id.empty_add_button)!!
                    addButton.setOnClickListener {
                        listener?.onAddButtonClicked(0L, date)
                    }
                } else {
                    emptyListVew.isVisible = false
                    recyclerView.isVisible = true
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