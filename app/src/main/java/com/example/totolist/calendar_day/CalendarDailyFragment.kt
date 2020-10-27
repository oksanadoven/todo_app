package com.example.totolist.calendar_day

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R
import com.example.totolist.calendar.CalendarListItem
import com.example.totolist.calendar.CalendarTaskCheckboxItem
import com.example.totolist.calendar.CalendarViewModel
import com.example.totolist.calendar.CalendarViewModelFactory
import com.example.totolist.database.TasksDatabase
import com.example.totolist.list_for_date.TaskListForDateFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class CalendarDailyFragment : Fragment() {

    interface OnMenuClickListener {
        fun onActionAddClicked(id: Long, date: String)
    }

    private lateinit var viewModel: CalendarViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var currentDate: TextView
    private lateinit var tasksTotal: TextView
    private val calendarInstance = Calendar.getInstance()
    private val dayCardAdapter = DayCardAdapter()
    private var daysList = mutableListOf<CalendarDay>().plus(addDay(calendarInstance.timeInMillis))
    private var selectedDate: String? = null
    private val startDate: String =
        SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(calendarInstance.time)
    var onClickListener: OnMenuClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = requireNotNull(this.activity).application
        val dataSource = TasksDatabase.getInstance(application).tasksDatabaseDao
        //Instance of the ViewModel Factory
        val viewModelFactory = CalendarViewModelFactory(dataSource, application)
        //Instance of the ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory).get(CalendarViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_calendar_daily, container, false)
        recyclerView = rootView.findViewById(R.id.daily_calendar_recycler_view)
        recyclerView.itemAnimator?.changeDuration = 50
        recyclerView.adapter = dayCardAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        val recyclerViewDivider =
            DividerItemDecoration(recyclerView.context, LinearLayoutManager.HORIZONTAL)
        val drawableDivider = ResourcesCompat.getDrawable(
            context!!.resources,
            R.drawable.day_card_divider,
            context!!.theme
        )
        recyclerViewDivider.setDrawable(drawableDivider!!)
        recyclerView.addItemDecoration(recyclerViewDivider)
        daysList = daysList.plus(loadMoreItems(15))
        dayCardAdapter.submitList(daysList)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastCompletelyVisibleItemPosition() == daysList.size - 1) {
                    daysList = daysList.plus(loadMoreItems(10))
                    dayCardAdapter.submitList(daysList)
                }
            }
        })
        dayCardAdapter.listener = object : DayCardAdapter.OnItemClickListener {
            override fun onDaySelected(day: CalendarDay) {
                val calendarForDate = Calendar.getInstance()
                calendarForDate.timeInMillis = day.dateInMs
                currentDate.text =
                    SimpleDateFormat("EEEE, MMM d", Locale.ROOT).format(calendarForDate.time)
                selectedDate = day.databaseDate
                calculateTotalTasks(day.databaseDate)
                openFragmentForDate(day.databaseDate)
            }
        }
        return rootView
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (selectedDate != null) {
            openFragmentForDate(selectedDate!!)
        } else {
            openFragmentForDate(startDate)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentDate = view.findViewById(R.id.current_day)
        tasksTotal = view.findViewById(R.id.tasks_total)
        currentDate.text =
            SimpleDateFormat("EEEE, MMM d", Locale.ROOT).format(Calendar.getInstance().time)
        calculateTotalTasks(
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.ROOT
            ).format(Calendar.getInstance().time)
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_daily_calendar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.daily_icon_action_add -> {
                if (selectedDate != null) {
                    onClickListener?.onActionAddClicked(0L, selectedDate!!)
                } else {
                    onClickListener?.onActionAddClicked(0L, startDate)
                }
                true
            }
            else -> false
        }
    }

    private fun loadMoreItems(numberOfItems: Int): List<CalendarDay> {
        var i = 0
        var newList = emptyList<CalendarDay>()
        while (i++ <= numberOfItems) {
            calendarInstance.add(Calendar.DATE, 1)
            val startMs = calendarInstance.timeInMillis
            newList = newList.plus(addDay(startMs))
        }
        return newList
    }

    private fun addDay(dateInMs: Long): CalendarDay {
        calendarInstance.timeInMillis = dateInMs
        val dateNumber = calendarInstance.get(Calendar.DAY_OF_MONTH).toString()
        val dayOfWeek = SimpleDateFormat("EE", Locale.ROOT).format(calendarInstance.time)
        val formattedDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(calendarInstance.time)
        return CalendarDay(
            dateNumber,
            dayOfWeek,
            dateInMs,
            formattedDate
        )
    }

    private fun calculateTotalTasks(date: String) {
        var items: List<CalendarListItem>?
        var taskItemCount = 0
        lifecycleScope.launch {
            items = viewModel.getCalendarItemsByDate(date)
            withContext(Dispatchers.Main) {
                for (item in items!!) {
                    if (item is CalendarTaskCheckboxItem) {
                        if (!item.item.isDone) taskItemCount++
                    }
                }
                if (taskItemCount > 0) {
                    val textBeginning = context!!.getText(R.string.tasks_total_beginning)
                    val textEnding = if (taskItemCount > 1) {
                        context!!.getText(R.string.tasks_total_ending_plural)
                    } else {
                        context!!.getText(R.string.tasks_total_ending_singular)
                    }
                    tasksTotal.text =
                        textBeginning.toString() + " " + taskItemCount + " " + textEnding
                } else {
                    tasksTotal.setText(R.string.no_tasks_total)
                }
            }
        }
    }

    private fun openFragmentForDate(date: String) {
        childFragmentManager.beginTransaction()
            .replace(R.id.daily_calendar_fragment_container, TaskListForDateFragment().apply {
                arguments = Bundle().apply {
                    putString(TaskListForDateFragment.ARG_TASK_DATE, date)
                }
            })
            .commit()
    }

}