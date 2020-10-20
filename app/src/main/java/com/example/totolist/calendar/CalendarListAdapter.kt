package com.example.totolist.calendar

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R

class CalendarListAdapter : ListAdapter<CalendarListItem, RecyclerView.ViewHolder>(
    TasksDiffCallback()
) {

    interface OnItemChecked {
        fun onItemChecked(itemId: Long, isDone: Boolean)
    }

    class CalendarHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun create(parent: ViewGroup): CalendarHeaderViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.calendar_task_header_item, parent, false)
                return CalendarHeaderViewHolder(itemView)
            }
        }

        private val listHeader: TextView = itemView.findViewById(R.id.calendar_task_header)
        fun bind(header: CalendarTaskHeaderItem) {
            listHeader.text = header.task.header
        }
    }

    class CalendarCheckboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun create(parent: ViewGroup): CalendarCheckboxViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.calendar_task_checkbox_item, parent, false)
                return CalendarCheckboxViewHolder(itemView)
            }
        }

        val checkbox: CheckBox = itemView.findViewById(R.id.calendar_task_checkbox)

        fun bind(taskItem: CalendarTaskCheckboxItem) {
            checkbox.isChecked = taskItem.item.isDone
            checkbox.text = taskItem.item.text
        }

    }

    class TasksDiffCallback : DiffUtil.ItemCallback<CalendarListItem>() {
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

    var listener: OnItemChecked? = null

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return item.getLayoutResId()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var holder: RecyclerView.ViewHolder
        when (viewType) {
            R.layout.calendar_task_header_item -> holder = CalendarHeaderViewHolder.create(parent)
            R.layout.calendar_task_checkbox_item -> {
                holder = CalendarCheckboxViewHolder.create(parent)
                holder.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                    buttonView.isChecked = isChecked
                    val currentItem = getItem(holder.adapterPosition) as CalendarTaskCheckboxItem
                    currentItem.item.isDone = buttonView.isChecked
                    if (currentItem.item.isDone) {
                        holder.checkbox.setTextColor(holder.checkbox.context.resources.getColor(R.color.colorTextSecondary))
                        holder.checkbox.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        holder.checkbox.setTextColor((holder.checkbox.context.resources.getColor(R.color.design_default_color_on_secondary)))
                        holder.checkbox.paintFlags = 0
                    }
                    submitList(currentList.sorted())
                    listener?.onItemChecked(currentItem.item.id, currentItem.item.isDone)
                }}
            else -> throw IllegalArgumentException("Unknown view type")
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (holder is CalendarHeaderViewHolder) {
            holder.bind(currentItem as CalendarTaskHeaderItem)
        } else if (holder is CalendarCheckboxViewHolder) {
            holder.bind(currentItem as CalendarTaskCheckboxItem)
        }
    }
}