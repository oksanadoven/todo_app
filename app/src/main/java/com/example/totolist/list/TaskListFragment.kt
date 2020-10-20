package com.example.totolist.list

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.totolist.R
import com.example.totolist.TaskListItem
import com.example.totolist.TasksDatabase
import com.example.totolist.utils.TaskListMode
import com.example.totolist.utils.TaskListMode.Normal
import com.example.totolist.utils.TaskListMode.Select
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*


class TaskListFragment : Fragment() {

    interface Listener {
        fun onTaskSelected(id: Long, date: String?)
    }

    var listener: Listener? = null
    private lateinit var viewModel: TodoListViewModel
    private val taskListAdapter: TaskListAdapter = TaskListAdapter()
    private var isSearchModeEnabled: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    val mode: LiveData<TaskListMode> = MediatorLiveData<TaskListMode>().apply {
        value = Normal
        addSource(taskListAdapter.getCheckedItems()) { items ->
            value = if (items.isNotEmpty()) {
                Select(selectedItemsCount = items.size)
            } else {
                Normal
            }
        }
        addSource(isSearchModeEnabled) {
            value = if (isSearchModeEnabled.value!!) {
                TaskListMode.Search
            } else {
                Normal
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        //Get instance of database
        val application = requireNotNull(this.activity).application
        val dataSource = TasksDatabase.getInstance(
            application
        ).tasksDatabaseDao
        //Create and instance of ViewModelFactory
        val viewModelFactory =
            TodoListViewModelFactory(
                dataSource,
                application
            )
        //reference of todoListViewModel
        viewModel =
            ViewModelProvider(activity!!, viewModelFactory).get(TodoListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_task_list, container, false)
        val fabAdd: FloatingActionButton = rootView.findViewById(R.id.fab_add)
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.itemAnimator?.changeDuration = 150
        //Create adapter and supply data to be displayed
        recyclerView.adapter = taskListAdapter
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        taskListAdapter.onItemClickListener = object :
            TaskListAdapter.OnItemClickListener {
            override fun onItemSelected(item: TaskListItem) {
                listener?.onTaskSelected(item.taskWithItems.task.id, SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Calendar.getInstance().time))
            }
        }
        fabAdd.setOnClickListener {
            listener?.onTaskSelected(0L, SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Calendar.getInstance().time))
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.taskItemsLiveData.observe(this, { taskList ->
            if (taskList != null) {
                taskListAdapter.setTasks(taskList)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val showDeleteIcon = mode.value is Select
        val showDiscardIcon = mode.value is TaskListMode.Search || mode.value is Select
        menu.findItem(R.id.action_delete_list).isVisible = showDeleteIcon
        menu.findItem(R.id.action_discard_selection).isVisible = showDiscardIcon
        menu.findItem(R.id.icon_action_search).isVisible = !showDeleteIcon
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_list -> {
                val itemsIdsToDelete: List<Long> =
                    taskListAdapter.getCheckedItems().value?.map { items ->
                        items.taskWithItems.task.id
                    } ?: return true
                viewModel.deleteItems(itemsIdsToDelete)
                taskListAdapter.selectModeOff()
                true
            }
            R.id.action_discard_selection -> {
                if (mode.value is Select) {
                    taskListAdapter.selectModeOff()
                }
                if (mode.value is TaskListMode.Search) {
                    isSearchModeEnabled.value = false
                    setQuery("")
                }
                true
            }
            R.id.icon_action_search -> {
                if (isSearchModeEnabled.value == false) {
                    isSearchModeEnabled.value = true
                }
                true
            }
            else -> false
        }
    }

    fun setQuery(text: String) {
        viewModel.setQuery(text)
    }

}
