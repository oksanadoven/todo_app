package com.example.totolist.search_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R
import com.example.totolist.month_fragment.CalendarListItem
import com.example.totolist.month_fragment.CalendarTaskCheckboxItem
import com.example.totolist.month_fragment.CalendarTaskHeaderItem

class SearchResultsChildAdapter: ListAdapter<CalendarListItem, RecyclerView.ViewHolder>(
    SearchDiffCallback()
) {
    class SearchDiffCallback: DiffUtil.ItemCallback<CalendarListItem>() {
        override fun areItemsTheSame(
            oldItem: CalendarListItem,
            newItem: CalendarListItem
        ): Boolean {
            return if (oldItem is CalendarTaskHeaderItem && newItem is CalendarTaskHeaderItem) {
                oldItem.task.id == newItem.task.id
            } else if (oldItem is CalendarTaskCheckboxItem && newItem is CalendarTaskCheckboxItem) {
                oldItem.item.id == newItem.item.id
            } else {
                false
            }
        }

        override fun areContentsTheSame(
            oldItem: CalendarListItem,
            newItem: CalendarListItem
        ): Boolean {
            return if (oldItem is CalendarTaskHeaderItem && newItem is CalendarTaskHeaderItem) {
                oldItem.task == newItem.task
            } else if (oldItem is CalendarTaskCheckboxItem && newItem is CalendarTaskCheckboxItem) {
                oldItem.item == newItem.item
            } else {
                false
            }
        }

    }

    class SearchHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun create(parent: ViewGroup): SearchHeaderViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.calendar_task_header_item, parent, false)
                return SearchHeaderViewHolder(itemView)
            }
        }

        private val listHeader: TextView = itemView.findViewById(R.id.calendar_task_header)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.calendar_task_header_delete_list)
        fun bind(header: CalendarTaskHeaderItem) {
            listHeader.text = header.task.header
            deleteIcon.isVisible = false
        }
    }

    class SearchCheckboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun create(parent: ViewGroup): SearchCheckboxViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.calendar_task_checkbox_item, parent, false)

                return SearchCheckboxViewHolder(itemView)
            }
        }

        private val checkbox: CheckBox = itemView.findViewById<CheckBox>(R.id.calendar_task_checkbox).apply {
            isClickable = false
        }

        fun bind(taskItem: CalendarTaskCheckboxItem) {
            checkbox.isChecked = taskItem.item.isDone
            checkbox.text = taskItem.item.text
        }

    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return item.getLayoutResId()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.calendar_task_header_item -> {
                val holder = SearchHeaderViewHolder.create(parent)
                holder
            }
            R.layout.calendar_task_checkbox_item -> {
                val holder = SearchCheckboxViewHolder.create(parent)
                holder
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (holder is SearchHeaderViewHolder) {
            holder.bind(currentItem as CalendarTaskHeaderItem)
        } else if (holder is SearchCheckboxViewHolder) {
            holder.bind(currentItem as CalendarTaskCheckboxItem)
        }
    }
}