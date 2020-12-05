package com.example.totolist.goups

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R
import com.example.totolist.database.Group
import com.example.totolist.database.TasksDatabase
import com.example.totolist.details.TaskDetailsFragment
import com.example.totolist.month_fragment.TaskGroupItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupsFragment : Fragment() {

    companion object {
        const val GROUP_ID = "GROUP_ID"
        const val TASK_ID = "TASK_ID"
    }

    interface SetGroupForTaskListener {
        fun onGroupSelected()
    }

    private lateinit var viewModel: GroupsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var newGroupEditText: EditText
    private lateinit var groupColorSpinner: Spinner
    private lateinit var fabSaveGroup: FloatingActionButton
    private val groupsAdapter = GroupsAdapter()
    private lateinit var dropDownAdapter: DropDownColorsAdapter
    var onGroupChosenListener: SetGroupForTaskListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val selectedGroupId = arguments?.getLong(GROUP_ID) ?: 0L
        val application = requireNotNull(this.activity).application
        val dataSource = TasksDatabase.getInstance(application).taskDBDao()
        val viewModelFactory = GroupsViewModel.Factory(dataSource, selectedGroupId)
        viewModel = ViewModelProvider(this, viewModelFactory).get(GroupsViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.groups_fragment_recycler_view)
        newGroupEditText = view.findViewById(R.id.fragment_group_name)
        groupColorSpinner = view.findViewById(R.id.group_color_spinner)
        fabSaveGroup = view.findViewById(R.id.fab_group_save)
        dropDownAdapter = DropDownColorsAdapter(requireContext())
        groupColorSpinner.adapter = dropDownAdapter
        recyclerView.adapter = groupsAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        viewModel.groupsLiveData.observe(viewLifecycleOwner, { groups ->
            setupColorsDropdown(groups)
            groupsAdapter.submitList(groups)
            addEmptyGroupField()
        })
        groupsAdapter.setGroupListener = object : GroupsAdapter.SetTaskItemGroupListener {
            override fun onGroupSelected(oldSelected: Group, newSelected: Group) {
                val taskId = arguments?.getLong(TASK_ID)
                lifecycleScope.launch {
                    viewModel.setSelectedTask(oldSelected, newSelected, taskId!!)
                }
            }
        }
        newGroupEditText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    val groupColor =
                        groupColorSpinner.getItemAtPosition(groupColorSpinner.selectedItemPosition)
                            .toString()
                    val newGroup =
                        Group(name = newGroupEditText.text.toString(), color = groupColor)
                    if (groupColor.isEmpty()) {
                        groupColorSpinner.performClick()
                        newGroup.copy(color = groupColor)
                    }
                    saveGroupAndAddEmptyField(newGroup)
                }
            }
            false
        }
        fabSaveGroup.setOnClickListener {
            setGroupForTaskAndLeave()
        }
    }

    private fun setGroupForTaskAndLeave() {
        val taskId = arguments?.getLong(TASK_ID)
        val selectedGroup = viewModel.groupsLiveData.value!!.find { it.isSelected }
            ?: viewModel.groupsLiveData.value!!.find { it.group.name == "No Group" }
        setResult(selectedGroup!!.group.groupId)
        if (taskId != 0L) {
            lifecycleScope.launch {
                viewModel.updateGroupForTask(selectedGroup.group.groupId, taskId)
                withContext(Dispatchers.Main) {
                    onGroupChosenListener?.onGroupSelected()
                }
            }
        } else {
            onGroupChosenListener?.onGroupSelected()
        }
    }

    private fun setResult(groupId: Long) {
        setFragmentResult(TaskDetailsFragment.REQUEST_KEY, Bundle().apply {
            putLong(TaskDetailsFragment.RESULT_GROUP_ID, groupId)
        })
    }

    private fun setupColorsDropdown(groups: List<TaskGroupItem>) {
        val allowedColors: Array<String> =
            requireActivity().resources.getStringArray(R.array.HexColors)
        val existingGroupColors = groups.map { item -> item.group.color }
        val availableColors = allowedColors.filter { color -> !existingGroupColors.contains(color) }
        dropDownAdapter.setColors(availableColors)
    }

    private fun saveGroupAndAddEmptyField(group: Group) {
        lifecycleScope.launch {
            viewModel.addNewGroup(group)
        }
        addEmptyGroupField()
    }

    private fun addEmptyGroupField() {
        newGroupEditText.setText("")
        groupColorSpinner.setSelection(0)
    }

}