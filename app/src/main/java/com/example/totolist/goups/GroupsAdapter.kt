package com.example.totolist.goups

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R
import com.example.totolist.database.Group
import com.example.totolist.month_fragment.TaskGroupItem

private val diffCallback = object : DiffUtil.ItemCallback<TaskGroupItem>() {

    override fun areItemsTheSame(oldItem: TaskGroupItem, newItem: TaskGroupItem): Boolean {
        return oldItem.group.groupId == newItem.group.groupId
    }

    override fun areContentsTheSame(oldItem: TaskGroupItem, newItem: TaskGroupItem): Boolean {
        return oldItem.group == newItem.group
    }

}

class GroupsAdapter : ListAdapter<TaskGroupItem, GroupsAdapter.GroupItemViewHolder>(diffCallback) {

    interface SetTaskItemGroupListener {
        fun onGroupSelected(oldSelected: Group, newSelected: Group)
    }

    class GroupItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun create(parent: ViewGroup): GroupItemViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.groups_list_item, parent, false)
                return GroupItemViewHolder(itemView)
            }
        }

        private val groupColor: ImageView = itemView.findViewById(R.id.rw_group_color)
        val groupCheckbox: CheckBox = itemView.findViewById(R.id.rw_group_checkbox)

        fun bind(item: TaskGroupItem) {
            groupColor.setColorFilter(Color.parseColor(item.group.color))
            groupCheckbox.isChecked = item.isSelected
            groupCheckbox.text = item.group.name
        }
    }

    var setGroupListener: SetTaskItemGroupListener? = null
    private var lastChecked: CheckBox? = null
    private var lastCheckedPos = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupItemViewHolder {
        val holder = GroupItemViewHolder.create(parent)
        holder.groupCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            val position = holder.adapterPosition
            val item = getItem(position)
            if (buttonView.isChecked == isChecked) {
                buttonView.isChecked = isChecked
                if (lastChecked != null) {
                    lastChecked!!.isChecked = false
                }
                lastChecked = holder.groupCheckbox
                lastCheckedPos = position
            } else {
                lastChecked = null
            }
            val previousSelected = currentList.find { it.isSelected }
                ?: currentList.find { it.group.name == "No Group" }
            previousSelected!!.isSelected = false
            item.isSelected = buttonView.isChecked
            setGroupListener?.onGroupSelected(previousSelected.group, item.group)
        }
        return holder
    }

    override fun onBindViewHolder(holder: GroupItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (position == 0 && holder.groupCheckbox.isChecked) {
            lastChecked = holder.groupCheckbox
            lastCheckedPos = 0
        }
        holder.bind(currentItem)
    }

}

