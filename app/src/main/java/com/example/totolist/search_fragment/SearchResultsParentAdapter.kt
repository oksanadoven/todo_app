package com.example.totolist.search_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R
import com.example.totolist.database.Task
import com.example.totolist.search_fragment.SearchResultsParentAdapter.SearchResultViewHolder

private val diffCallback = object : DiffUtil.ItemCallback<SearchListItem>() {
    override fun areItemsTheSame(oldItem: SearchListItem, newItem: SearchListItem): Boolean {
        return oldItem.children == newItem.children
    }

    override fun areContentsTheSame(oldItem: SearchListItem, newItem: SearchListItem): Boolean {
        return oldItem == newItem
    }
}

class SearchResultsParentAdapter :
    ListAdapter<SearchListItem, SearchResultViewHolder>(diffCallback) {

    interface OpenDetailsScreenListener {
        fun onOpenSelectedListRequested(item: Task)
    }

    class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val recyclerView: RecyclerView = itemView.findViewById(R.id.search_tasks_list)

        init {
            recyclerView.adapter = SearchResultsChildAdapter()
            recyclerView.layoutManager = LinearLayoutManager(itemView.context)
        }

        fun bind(item: SearchListItem) {
            (recyclerView.adapter as SearchResultsChildAdapter).submitList(item.children)
        }
    }

    var openDetailsListener: OpenDetailsScreenListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.search_card_item, parent, false)
        val holder = SearchResultViewHolder(itemView)
        itemView.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                val item = getItem(holder.adapterPosition)
                openDetailsListener?.onOpenSelectedListRequested(item.task)
            }
        }
        return holder

    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}