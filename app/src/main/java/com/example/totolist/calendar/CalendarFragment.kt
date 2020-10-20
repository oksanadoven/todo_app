package com.example.totolist.calendar

import android.os.Bundle
import android.view.*
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R
import com.example.totolist.TasksDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    interface Listener {
        fun onActionAddSelected(id: Long, date: String?)
    }

    private lateinit var viewModel: CalendarViewModel
    private lateinit var calendar: CalendarView
    private lateinit var currentDate: TextView
    private lateinit var recyclerView: RecyclerView
    private val calendarListAdapter: CalendarListAdapter = CalendarListAdapter()
    var listener: Listener? = null
    private var date: MutableLiveData<String> = MutableLiveData<String>().apply {
        value = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Calendar.getInstance().time)
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
        val calendarInstance = Calendar.getInstance()
        calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendarInstance.set(year, month, dayOfMonth)
            val dateFormatter = SimpleDateFormat("EEEE d, MMM", Locale.ROOT)
            val formattedDate = dateFormatter.format(calendarInstance.time)
            date.value = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(calendarInstance.time)
            currentDate.text = formattedDate
            syncCalendarItemsWithDate()
        }
        recyclerView.adapter = calendarListAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        syncCalendarItemsWithDate()
        calendarListAdapter.listener = object : CalendarListAdapter.OnItemChecked {
            override fun onItemChecked(itemId: Long, isDone: Boolean) {
                lifecycleScope.launch {
                    viewModel.updateTaskItems(itemId, isDone)
                }
            }
        }
    }

    private fun syncCalendarItemsWithDate() {
        lifecycleScope.launch {
            val items = viewModel.getCalendarItemsByDate(date.value!!)
            withContext(Dispatchers.Main) {
                calendarListAdapter.submitList(items)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calendar = view.findViewById(R.id.calendar_view)
        currentDate = view.findViewById(R.id.current_date)
        recyclerView = view.findViewById(R.id.calendar_recycler_view)
        calendar.setDate(System.currentTimeMillis(), false, true)
        currentDate.text = SimpleDateFormat("EEEE d, MMM", Locale.ROOT).format(calendar.date)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.icon_action_add -> {
                listener?.onActionAddSelected(0L, date.value)
                true
            }
            else -> false
        }
    }

    companion object
}