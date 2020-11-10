package com.example.totolist.search_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R
import com.example.totolist.task_list_for_date_fragment.CalendarListAdapter

class SearchResultsAdapter : ListAdapter<SearchListItem, SearchResultsAdapter.SearchResultViewHolder>(
    TasksDiffCallback()
) {
    private class TasksDiffCallback : DiffUtil.ItemCallback<SearchListItem>() {
        override fun areItemsTheSame(oldItem: SearchListItem, newItem: SearchListItem): Boolean {
            return oldItem.children == newItem.children
        }

        override fun areContentsTheSame(oldItem: SearchListItem, newItem: SearchListItem): Boolean {
            return oldItem == newItem
        }
    }

    class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val recyclerView: RecyclerView = itemView.findViewById(R.id.search_tasks_list)

        init {
            recyclerView.adapter = CalendarListAdapter()
            recyclerView.layoutManager = LinearLayoutManager(itemView.context)
        }

        fun bind(item: SearchListItem) {
            (recyclerView.adapter as CalendarListAdapter).submitList(item.children)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.search_card_item, parent, false)
        return SearchResultViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}