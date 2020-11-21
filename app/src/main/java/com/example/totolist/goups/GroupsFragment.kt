package com.example.totolist.goups

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R
import com.example.totolist.database.Group
import com.example.totolist.database.TasksDatabase
import kotlinx.coroutines.launch

class GroupsFragment : Fragment() {

    companion object {
        const val GROUP_ID = "GROUP_ID"
    }

    private lateinit var viewModel: GroupsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupColorSelector: ImageView
    private lateinit var newGroupEditText: EditText
    private val groupsAdapter = GroupsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val application = requireNotNull(this.activity).application
        val dataSource = TasksDatabase.getInstance(application).taskDBDao()
        val viewModelFactory = GroupsViewModelFactory(dataSource)
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
        groupColorSelector = view.findViewById(R.id.fragment_group_color)
        newGroupEditText = view.findViewById(R.id.fragment_group_name)
        val selectedGroup = arguments?.getLong(GROUP_ID)
        recyclerView.adapter = groupsAdapter
        groupsAdapter.submitList(
            viewModel.populateTaskGroupList(selectedGroup!!)
        )
        groupsAdapter.setGroupListener = object : GroupsAdapter.SetTaskItemGroupListener {
            override fun onGroupSelected(oldSelected: Group?, newSelected: Group) {
                if (oldSelected?.groupId != 0L) {
                    lifecycleScope.launch {
                        viewModel.updateGroup(oldSelected!!)
                    }
                }
                lifecycleScope.launch {
                    viewModel.updateGroup(newSelected)
                }
            }
        }
    }

}