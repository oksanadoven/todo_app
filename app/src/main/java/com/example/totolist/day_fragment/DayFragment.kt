package com.example.totolist.day_fragment

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
import com.example.totolist.database.TasksDatabase
import com.example.totolist.month_fragment.CalendarViewModel
import com.example.totolist.month_fragment.CalendarViewModelFactory
import com.example.totolist.task_list_for_date_fragment.TaskListForDateFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

class DayFragment : Fragment() {

    interface OnMenuClickListener {
        fun onActionAddClicked(id: Long, date: Long)
    }

    private lateinit var viewModel: CalendarViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var currentDate: TextView
    private lateinit var tasksTotal: TextView
    private val dayCardAdapter = DayCardAdapter()
    var onClickListener: OnMenuClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = requireNotNull(this.activity).application
        val dataSource = TasksDatabase.getInstance(application).tasksDatabaseDao
        val viewModelFactory = CalendarViewModelFactory(dataSource)
        viewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(CalendarViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
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
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastCompletelyVisibleItemPosition() == dayCardAdapter.itemCount - 1) {
                    val lastDayInList = dayCardAdapter.currentList.last()
                    val nextDay = Instant
                        .ofEpochMilli(lastDayInList.timeInMillis)
                        .atOffset(ZoneOffset.UTC)
                        .plus(1L, ChronoUnit.DAYS)
                        .toInstant()
                        .toEpochMilli()
                    val newList = dayCardAdapter.currentList + newItemsList(nextDay)
                    dayCardAdapter.submitList(newList)
                }
            }
        })
        dayCardAdapter.listener = object : DayCardAdapter.OnItemClickListener {
            override fun onDaySelected(day: CalendarDay) {
                val dateText = Instant.ofEpochMilli(day.timeInMillis).atZone(ZoneId.systemDefault())
                    .toLocalDate().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
                currentDate.text = dateText
                if (viewModel.dateLiveData.value != day.timeInMillis) {
                    calculateTotalTasks(day.timeInMillis)
                    viewModel.setDate(day.timeInMillis)
                }
            }
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentDate = view.findViewById(R.id.current_day)
        tasksTotal = view.findViewById(R.id.tasks_total)
        val currentDay = Instant.ofEpochMilli(viewModel.dateLiveData.value!!)
        currentDate.text = currentDay.atZone(ZoneId.systemDefault()).toLocalDate()
            .format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
        calculateTotalTasks(currentDay.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli())
        viewModel.dateLiveData.observe(this, { newDate ->
            currentDate.text = Instant.ofEpochMilli(newDate).atZone(ZoneId.systemDefault())
                .toLocalDate().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
            val newDateUTC =
                Instant.ofEpochMilli(newDate).atZone(ZoneId.systemDefault()).toLocalDate()
                    .atStartOfDay()
                    .atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
            calculateTotalTasks(newDateUTC)
            val items = newItemsListWithOffset(newDate)
            dayCardAdapter.submitList(items) {
                recyclerView.scrollToPosition(items.size / 2)
            }
            openFragmentForDate(newDateUTC)
        })
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        if (childFragment is TaskListForDateFragment) {
            childFragment.taskListListener = object : TaskListForDateFragment.TaskListListener {
                override fun openDetailsScreenRequested(id: Long, date: Long) {
                    onClickListener?.onActionAddClicked(id, viewModel.dateLiveData.value!!)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_new_task -> {
                onClickListener?.onActionAddClicked(0L, viewModel.dateLiveData.value!!)
                true
            }
            else -> false
        }
    }

    private fun newItemsListWithOffset(startDate: Long): ArrayList<CalendarDay> {
        val daysList = ArrayList<CalendarDay>()
        val startDateOffset =
            Instant.ofEpochMilli(startDate).minus(15L, ChronoUnit.DAYS).toEpochMilli()
        var day = createDay(startDateOffset, false)
        daysList.add(day)
        repeat(30) {
            val nextDay = createNext(day, startDate)
            daysList.add(nextDay)
            day = nextDay
        }
        return daysList
    }

    private fun newItemsList(startDate: Long): ArrayList<CalendarDay> {
        val daysList = ArrayList<CalendarDay>()
        var day = createDay(startDate, false)
        daysList.add(day)
        repeat(30) {
            val nextDay = createNext(day, startDate)
            daysList.add(nextDay)
            day = nextDay
        }
        return daysList
    }

    private fun createNext(day: CalendarDay, startDate: Long): CalendarDay {
        val nextDay = Instant.ofEpochMilli(day.timeInMillis).plus(1, ChronoUnit.DAYS).toEpochMilli()
        return createDay(nextDay, isSelected = nextDay == startDate)
    }

    private fun createDay(timeInMillis: Long, isSelected: Boolean): CalendarDay {
        val zonedDate = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(timeInMillis),
            ZoneId.systemDefault()
        )
        val dateNumber = zonedDate.dayOfMonth.toString()
        val dayOfWeek = zonedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ROOT)
        return CalendarDay(
            dateNumber,
            dayOfWeek,
            timeInMillis,
            isSelected
        )
    }

    private fun calculateTotalTasks(date: Long) {
        var taskItemCount = 0
        lifecycleScope.launch {
            taskItemCount = viewModel.calculateTotalTasksForDay(date)
            withContext(Dispatchers.Main) {
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

    private fun openFragmentForDate(date: Long) {
        childFragmentManager.beginTransaction()
            .replace(R.id.daily_calendar_fragment_container, TaskListForDateFragment().apply {
                arguments = Bundle().apply {
                    putLong(
                        TaskListForDateFragment.ARG_TASK_DATE,
                        date
                    )
                }
            })
            .commit()
    }

    companion object {
        private const val ONE_DAY_IN_MILLIS = 60 * 60 * 24 * 1000
    }
}