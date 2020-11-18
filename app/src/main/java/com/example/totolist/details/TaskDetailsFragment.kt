package com.example.totolist.details

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R
import com.example.totolist.database.Task
import com.example.totolist.database.TaskItem
import com.example.totolist.database.TaskWithItems
import com.example.totolist.database.TasksDatabase
import com.example.totolist.utils.TaskItemDivider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter

class TaskDetailsFragment : Fragment() {

    interface SaveItemListener {
        fun onItemSaved()
    }

    interface TaskDeleteListener {
        fun onTaskDeleted()
    }

    private lateinit var viewModel: TodoDetailsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabSave: FloatingActionButton
    private lateinit var headerText: EditText
    private lateinit var dateHeader: TextView
    private lateinit var taskWithItems: TaskWithItems
    var saveItemListener: SaveItemListener? = null
    var listener: TaskDeleteListener? = null
    private val taskDetailsAdapter: TaskDetailsAdapter = TaskDetailsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val application = requireNotNull(this.activity).application
        val dataSource = TasksDatabase.getInstance(
            application
        ).tasksDatabaseDao
        val viewModelFactory =
            TodoDetailsViewModelFactory(
                dataSource,
                application
            )
        viewModel = ViewModelProvider(this, viewModelFactory).get(TodoDetailsViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_details, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.details_recycler_view)
        headerText = view.findViewById(R.id.edit_text_header)
        fabSave = view.findViewById(R.id.fab_save)
        dateHeader = view.findViewById(R.id.date_header)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerView.adapter = taskDetailsAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(TaskItemDivider(activity as Activity))
        fabSave.setOnClickListener {
            saveTaskAndLeave()
        }
        val taskId =
            arguments?.getLong(ARG_TASK_ID) ?: throw IllegalArgumentException("No task id provided")
        val date = arguments?.getLong(ARG_TASK_DATE)
        val dateUTC =
            Instant.ofEpochMilli(date!!).atZone(ZoneId.of("UTC")).toLocalDate().atStartOfDay()
                .toInstant(
                    ZoneOffset.UTC
                ).toEpochMilli()
        if (taskId != 0L) {
            lifecycleScope.launch {
                taskWithItems = viewModel.getTaskWithItems(taskId)
                renderTask(taskWithItems)
            }
        } else {
            taskWithItems = TaskWithItems(
                Task(
                    header = "",
                    date = dateUTC
                ), emptyList()
            )
            renderTask(taskWithItems)
        }
        val currentDate = Instant.ofEpochMilli(date).atOffset(ZoneOffset.UTC)
            .toLocalDate().format(DateTimeFormatter.ofPattern("MMM d"))
        dateHeader.text = currentDate
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                showDeleteTaskConfirmationDialog(taskWithItems.task)
                true
            }
            else -> false
        }
    }

    private fun showDeleteTaskConfirmationDialog(task: Task) {
        AlertDialog.Builder(context)
            .setMessage("Do you want to delete this list?")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                lifecycleScope.launch {
                    viewModel.deleteTaskWithItems(task)
                    withContext(Dispatchers.Main) {
                        listener?.onTaskDeleted()
                    }
                }
                return@OnClickListener
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                return@OnClickListener
            })
            .show()
    }

    private fun renderTask(taskWithItems: TaskWithItems) {
        if (taskWithItems.task.header.isNotEmpty()) {
            headerText.setText(taskWithItems.task.header)
        }
        if (taskWithItems.items.isNotEmpty()) {
            taskDetailsAdapter.submitList(taskWithItems.items)
            taskDetailsAdapter.addEmptyTaskItem()
        } else {
            taskDetailsAdapter.submitList(listOf(TaskItem(text = "", isDone = false)))
        }
    }

    private fun saveTaskAndLeave() {
        lifecycleScope.launch {
            var newHeader = headerText.text.toString()
            if (newHeader.isEmpty()) {
                newHeader = "New list"
            }
            val task = taskWithItems.task.copy(header = newHeader)
            val taskId = if (task.id == 0L) {
                viewModel.insert(task)
            } else {
                task.id
            }
            val items = taskDetailsAdapter.currentList
                .filter { it.text.isNotEmpty() }
                .map { taskItem ->
                    taskItem.copy(taskId = taskId)
                }
            viewModel.deleteAndInsert(task, items)
            withContext(Dispatchers.Main) {
                saveItemListener?.onItemSaved()
            }
        }
    }

    companion object {
        const val ARG_TASK_ID = "ARG_TASK_ID"
        const val ARG_TASK_DATE = "ARG_TASK_DATE"
    }
}