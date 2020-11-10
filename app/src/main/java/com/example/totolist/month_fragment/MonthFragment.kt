package com.example.totolist.month_fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.totolist.R
import com.example.totolist.database.TasksDatabase
import com.example.totolist.task_list_for_date_fragment.TaskListForDateFragment
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

    interface SearchScreenListener {
        fun searchActionSelected()
    }

    private lateinit var viewModel: CalendarViewModel
    private lateinit var calendar: CalendarView
    private lateinit var currentDate: TextView
    var listener: Listener? = null
    var searchListener: SearchScreenListener? = null

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
        val rootView = inflater.inflate(R.layout.fragment_calendar_view, container, false)
        viewModel.dateLiveData.observe(this, { newDate ->
            val newDateUTC = Instant.ofEpochMilli(newDate).atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay()
                .atOffset(UTC).toInstant().toEpochMilli()
            openFragmentForDate(newDateUTC)
            calendar.date = newDate
            currentDate.text = Instant.ofEpochMilli(newDate).atZone(ZoneId.systemDefault())
                .toLocalDate().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
        })
        return rootView
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.icon_action_add -> {
                listener?.onActionAddSelected(0L, viewModel.dateLiveData.value!!)
                true
            }
            R.id.icon_action_search -> {
                searchListener?.searchActionSelected()
                true
            }
            else -> false
        }
    }

    private fun openFragmentForDate(date: Long) {
        //Log.d("AAA", "open fragment for date ${Instant.ofEpochMilli(date).atZone(ZoneId.of("UTC")).toLocalDate()}")
        childFragmentManager.beginTransaction()
            .replace(R.id.calendar_fragment_container, TaskListForDateFragment().apply {
                arguments = Bundle().apply {
                    putLong(
                        TaskListForDateFragment.ARG_TASK_DATE, date)
                }
            })
            .commit()
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        if (childFragment is TaskListForDateFragment) {
            childFragment.taskListListener = object : TaskListForDateFragment.TaskListListener {
                override fun onAddListRequested(id: Long, date: Long) {
                    listener?.onActionAddSelected(id, viewModel.dateLiveData.value!!)
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