package com.example.totolist.month_fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.totolist.R
import com.example.totolist.database.TasksDatabase
import com.example.totolist.task_list_for_date_fragment.TaskListForDateFragment
import com.example.totolist.utils.TaskListMode
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset.UTC
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

class MonthFragment : Fragment() {

    interface Listener {
        fun onActionAddSelected(id: Long, date: Long)
    }

    private lateinit var viewModel: CalendarViewModel
    private lateinit var calendar: CalendarView
    private lateinit var currentDate: TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val application = requireNotNull(this.activity).application
        val dataSource = TasksDatabase.getInstance(application).tasksDatabaseDao
        //Instance of the ViewModel Factory
        val viewModelFactory = CalendarViewModelFactory(dataSource)
        //Instance of the ViewModel
        viewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(CalendarViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_calendar_fagment, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootview = inflater.inflate(R.layout.fragment_calendar_view, container, false)
        viewModel.dateLiveData.observe(this, { newDate ->
            openFragmentForDate(newDate)
            //val newDateWithOffset = ZonedDateTime.ofInstant(Instant.ofEpochMilli(newDate), ZoneId.systemDefault()).toInstant().toEpochMilli()
            calendar.date = newDate
            currentDate.text = Instant.ofEpochMilli(newDate).atZone(ZoneId.of(UTC.toString()))
                .toLocalDate().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
                .format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
        })
        return rootview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calendar = view.findViewById(R.id.calendar_view)
        currentDate = view.findViewById(R.id.current_date)
        calendar.setDate(viewModel.dateLiveData.value!!, false, true)
        currentDate.text =
            Instant.ofEpochMilli(viewModel.dateLiveData.value!!).atZone(ZoneId.of(UTC.toString()))
                .toLocalDate().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
        calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val monthText = if (month < 10) {
                if (month < 9) {
                    "0${month + 1}"
                } else {
                    "10"
                }
            } else {
                "${month + 1}"
            }
            val dayText = if (dayOfMonth < 10) {
                "0$dayOfMonth"
            } else {
                "$dayOfMonth"
            }
            val formatter = "$year-$monthText-$dayText"
            val selectedDate = LocalDate.parse(formatter).atStartOfDay()
            val formattedDate = LocalDate.parse(formatter)
                .format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
            viewModel.setDate(
                selectedDate.toInstant(
                    ZonedDateTime.of(selectedDate, ZoneId.systemDefault()).offset
                ).toEpochMilli()
            )
            currentDate.text = formattedDate
        }
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
                listener?.onActionAddSelected(0L, viewModel.dateLiveData.value!!)
                true
            }
            else -> false
        }
    }

    private fun openFragmentForDate(date: Long) {
        childFragmentManager.beginTransaction()
            .replace(R.id.calendar_fragment_container, TaskListForDateFragment().apply {
                arguments = Bundle().apply {
                    putLong(TaskListForDateFragment.ARG_TASK_DATE, date)
                }
            })
            .commit()
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        if (childFragment is TaskListForDateFragment) {
            childFragment.taskListListener = object : TaskListForDateFragment.TaskListListener {
                override fun onAddListRequested(id: Long, date: Long) {
                    listener?.onActionAddSelected(id, date)
                }
            }
            childFragment.imageListener = object : TaskListForDateFragment.ImageResizeListener {
                override fun resetImageSize(image: ImageView) {
                    image.requestLayout()
                    image.layoutParams.height = convertPxToDp(context!!, 700F).toInt()
                    image.layoutParams.width = convertPxToDp(context!!, 700F).toInt()
                }
            }
        }
    }

    fun convertPxToDp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }
}