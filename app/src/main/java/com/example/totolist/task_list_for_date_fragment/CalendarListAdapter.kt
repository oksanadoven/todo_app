package com.example.totolist.task_list_for_date_fragment

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R
import com.example.totolist.database.Task
import com.example.totolist.month_fragment.CalendarListItem
import com.example.totolist.month_fragment.CalendarTaskCheckboxItem
import com.example.totolist.month_fragment.CalendarTaskHeaderItem
import com.example.totolist.month_fragment.TaskGroupItem

private val diffCallback = object : DiffUtil.ItemCallback<CalendarListItem>() {
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

class CalendarListAdapter : ListAdapter<CalendarListItem, RecyclerView.ViewHolder>(diffCallback) {

    interface OnItemChecked {
        fun onItemChecked(itemId: Long, isDone: Boolean)
    }

    interface DeleteListListener {
        fun onActionDeleteChecked(task: Task)
    }

    interface OpenDetailsScreenListener {
        fun onListSelectedListener(task: Task)
    }

    class CalendarHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun create(parent: ViewGroup): CalendarHeaderViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.calendar_task_header_item, parent, false)
                return CalendarHeaderViewHolder(itemView)
            }
        }

        val listHeader: TextView = itemView.findViewById(R.id.calendar_task_header)
        val deleteIcon: ImageView = itemView.findViewById(R.id.calendar_task_header_delete_list)
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

    class TaskGroupItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup): TaskGroupItemViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.calendar_task_group_item, parent, false)
                return TaskGroupItemViewHolder(itemView)
            }
        }

        private val groupCardView: CardView = itemView.findViewById(R.id.group_card_item)
        private val groupName: TextView = itemView.findViewById(R.id.group_name)

        fun bind(item: TaskGroupItem) {
            groupName.text = item.group.name
            groupCardView.setCardBackgroundColor(Color.parseColor(item.group.color))
        }
    }

    var listener: OnItemChecked? = null
    var deleteListListener: DeleteListListener? = null
    var openDetailsScreenListener: OpenDetailsScreenListener? = null

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return item.getLayoutResId()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.calendar_task_header_item -> {
                val holder = CalendarHeaderViewHolder.create(parent)
                holder.deleteIcon.setOnClickListener {
                    val currentItem = getItem(holder.adapterPosition) as CalendarTaskHeaderItem
                    deleteListListener?.onActionDeleteChecked(currentItem.task)
                }
                holder.listHeader.setOnClickListener {
                    val currentItem = getItem(holder.adapterPosition) as CalendarTaskHeaderItem
                    openDetailsScreenListener?.onListSelectedListener(currentItem.task)
                }
                holder
            }
            R.layout.calendar_task_checkbox_item -> {
                val holder = CalendarCheckboxViewHolder.create(parent)
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
                }
                holder
            }
            R.layout.calendar_task_group_item -> {
                TaskGroupItemViewHolder.create(parent)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = getItem(position)
        when (holder) {
            is CalendarHeaderViewHolder -> {
                holder.bind(currentItem as CalendarTaskHeaderItem)
            }
            is CalendarCheckboxViewHolder -> {
                holder.bind(currentItem as CalendarTaskCheckboxItem)
            }
            is TaskGroupItemViewHolder -> {
                holder.bind(currentItem as TaskGroupItem)
            }
        }
    }
}