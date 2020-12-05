package com.example.totolist.details

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R
import com.example.totolist.database.*
import com.example.totolist.utils.TaskItemDivider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.io.Serializable

// TODO Добавить state(? - нужен research), который будет контролировать, чтобы у меня не создавались лишние поля, когда я возвращаюсь из GroupsFragment на DetailsFragment
// TODO Закончить SavedInstanceState() в onCreate() -> добавить связку данных с UI
// TODO не делать taskId как LiveData, а передать его как аргумент во ViewModel

class TaskDetailsFragment : Fragment() {

    interface SaveItemListener {
        fun onItemSaved()
    }

    interface TaskDeleteListener {
        fun onTaskDeleted()
    }

    interface AddGroupListener {
        fun onAddGroupForTask(groupId: Long, taskId: Long)
    }

    private lateinit var viewModel: TodoDetailsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabSave: FloatingActionButton
    private lateinit var headerText: EditText
    private lateinit var dateHeader: TextView
    private lateinit var groupLabel: CardView
    private lateinit var groupName: TextView
    var saveItemListener: SaveItemListener? = null
    var listener: TaskDeleteListener? = null
    var addGroupListener: AddGroupListener? = null
    private val taskDetailsAdapter: TaskDetailsAdapter = TaskDetailsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        setUpResultListener()
        if (savedInstanceState != null) {
            val taskWithItems = savedInstanceState.getSerializable(SAVED_TASK_WITH_ITEMS)
            val adapterList = savedInstanceState.getSerializable(SAVED_ADAPTER_TASK_LIST)
            val headerText = savedInstanceState.getString(SAVED_HEADER_TEXT)
        }
        val taskId =
            arguments?.getLong(ARG_TASK_ID) ?: throw IllegalArgumentException("No task id provided")
        val application = requireNotNull(this.activity).application
        val dataSource = TasksDatabase.getInstance(
            application
        ).taskDBDao()
        val viewModelFactory = TodoDetailsViewModel.Factory(dataSource)
        viewModel = ViewModelProvider(this, viewModelFactory).get(TodoDetailsViewModel::class.java)
        val dateUTC = Instant
            .ofEpochMilli(arguments?.getLong(ARG_TASK_DATE)!!)
            .atZone(ZoneId.of("UTC"))
            .toLocalDate().atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
        viewModel.setTaskId(taskId)
        viewModel.setDate(dateUTC)
    }

    private fun setUpResultListener() {
        setFragmentResultListener(REQUEST_KEY) { _, result ->
            val groupId = result.getLong(RESULT_GROUP_ID)
            viewModel.setGroupId(groupId)
        }
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
        groupLabel = view.findViewById(R.id.details_group_item)
        groupName = view.findViewById(R.id.details_group_name)
        val date = arguments?.getLong(ARG_TASK_DATE)
        val currentDate = Instant.ofEpochMilli(date!!).atOffset(ZoneOffset.UTC)
            .toLocalDate().format(DateTimeFormatter.ofPattern("MMM d"))
        dateHeader.text = currentDate
        recyclerView.adapter = taskDetailsAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(TaskItemDivider(activity as Activity))
        fabSave.setOnClickListener {
            saveTaskAndLeave()
        }
        viewModel.group.observe(viewLifecycleOwner, { group ->
            displayGroup(group)
        })
        viewModel.taskWithItems.observe(viewLifecycleOwner, { newTaskWithItems ->
            renderTask(newTaskWithItems)
        })
        groupLabel.setOnClickListener {
            addGroupListener?.onAddGroupForTask(
                viewModel.taskWithItems.value!!.task.taskGroupId,
                viewModel.taskWithItems.value!!.task.id
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val listItems = taskDetailsAdapter.currentList as Serializable
        outState.putSerializable(SAVED_TASK_WITH_ITEMS, viewModel.taskWithItems.value)
        outState.putSerializable(SAVED_ADAPTER_TASK_LIST, listItems)
        outState.putString(SAVED_HEADER_TEXT, headerText.toString())
    }

    private fun displayGroup(group: Group) {
        val groupColor = Color.parseColor(group.color)
        groupLabel.setCardBackgroundColor(groupColor)
        groupName.text = group.name
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                showDeleteTaskConfirmationDialog(viewModel.taskWithItems.value!!.task)
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
        if (taskWithItems.group?.name == "No Group") {
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
            var groupId = viewModel.group.value?.groupId
            if (groupId == null) {
                groupId = 0L
            }
            val task =
                viewModel.taskWithItems.value!!.task.copy(header = newHeader, taskGroupId = groupId)
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
        const val REQUEST_KEY = "REQUEST_KEY"
        const val RESULT_GROUP_ID = "RESULT_GROUP_ID"
        const val SAVED_TASK_WITH_ITEMS = "SAVED_TASK_WITH_ITEMS"
        const val SAVED_ADAPTER_TASK_LIST = "ADAPTER_TASK_LIST"
        const val SAVED_HEADER_TEXT = "SAVED_HEADER_TEXT"
    }
}