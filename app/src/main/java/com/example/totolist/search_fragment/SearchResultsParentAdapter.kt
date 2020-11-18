package com.example.totolist.search_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.*
import com.example.totolist.R
import com.example.totolist.database.Task

private val diffCallback = object : DiffUtil.ItemCallback<SearchItem>() {
    override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
        return if (oldItem is SearchListItem && newItem is SearchListItem) {
            oldItem.task.id == newItem.task.id
        } else if (oldItem is SearchHeaderItem && newItem is SearchHeaderItem) {
            oldItem.date == newItem.date
        } else {
            false
        }
    }

    override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
        return if (oldItem is SearchListItem && newItem is SearchListItem) {
            oldItem.children == newItem.children
        } else if (oldItem is SearchHeaderItem && newItem is SearchHeaderItem) {
            oldItem.date == newItem.date
        } else {
            false
        }
    }
}

class SearchResultsParentAdapter :
    ListAdapter<SearchItem, RecyclerView.ViewHolder>(diffCallback) {

    interface OpenDetailsScreenListener {
        fun onOpenSelectedListRequested(item: Task)
    }

    class SearchItemsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup): SearchItemsViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.search_card_item, parent, false)
                return SearchItemsViewHolder(itemView)
            }
        }

        private val recyclerView: RecyclerView = itemView.findViewById(R.id.search_tasks_list)

        init {
            recyclerView.adapter = SearchResultsChildAdapter()
            recyclerView.layoutManager = LinearLayoutManager(itemView.context)
        }

        fun bind(item: SearchListItem) {
            (recyclerView.adapter as SearchResultsChildAdapter).submitList(item.children)
        }
    }

    class SearchDateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun create(parent: ViewGroup): SearchDateHeaderViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.search_card_date_header, parent, false)
                return SearchDateHeaderViewHolder(itemView)
            }
        }

        private val dateHeader = itemView.findViewById<TextView>(R.id.search_date_header)

        fun bind(item: SearchHeaderItem) {
            dateHeader.text = item.getDateString(item.date)
        }
    }

    var openDetailsListener: OpenDetailsScreenListener? = null

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return item.getLayoutResId()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.search_card_date_header -> {
                SearchDateHeaderViewHolder.create(parent)
            }
            R.layout.search_card_item -> {
                val holder = SearchItemsViewHolder.create(parent)
                holder.itemView.setOnClickListener {
                    if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                        val item = getItem(holder.adapterPosition) as SearchListItem
                        openDetailsListener?.onOpenSelectedListRequested(item.task)
                    }
                }
                holder
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder.itemViewType == R.layout.search_card_date_header) {
            setFullSpan(holder)
        }
    }

    private fun setFullSpan(holder: RecyclerView.ViewHolder) {
        if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            val params = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            params.isFullSpan = true
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (holder is SearchItemsViewHolder) {
            holder.bind(currentItem as SearchListItem)
        } else if (holder is SearchDateHeaderViewHolder) {
            holder.bind(currentItem as SearchHeaderItem)
        }
    }
}