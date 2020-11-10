package com.example.totolist.search_fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.totolist.R
import com.example.totolist.database.TasksDatabase

class SearchFragment : Fragment() {

    interface SearchDiscardListener {
        fun searchDiscardRequested()
    }

    private lateinit var viewModel: SearchViewModel
    private lateinit var recyclerView: RecyclerView
    private val searchResultsAdapter = SearchResultsAdapter()
    var listener: SearchDiscardListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val application = requireNotNull(this.activity).application
        val dataSource = TasksDatabase.getInstance(application).tasksDatabaseDao
        val viewModelFactory = SearchViewModelFactory(dataSource)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)
            .get(SearchViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_search, container, false)
        recyclerView = rootView.findViewById(R.id.search_screen_recycler_view)
        recyclerView.adapter = searchResultsAdapter
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        viewModel.taskItemsLiveData.observe(this, { taskList ->
            searchResultsAdapter.submitList(taskList)
        })
        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_discard_selection -> {
                listener?.searchDiscardRequested()
                true
            }
            else -> false
        }
    }

    fun setQuery(text: String) {
        viewModel.setQuery(text)
    }
}