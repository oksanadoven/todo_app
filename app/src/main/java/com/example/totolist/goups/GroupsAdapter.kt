package com.example.totolist.goups

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
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
        fun onGroupSelected(oldSelected: Group?, newSelected: Group)
    }

    class GroupItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun create(parent: ViewGroup) : GroupItemViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.groups_list_item, parent, false)
                return GroupItemViewHolder(itemView)
            }
        }

        private val groupColor : ImageView = itemView.findViewById(R.id.rw_group_color)
        private val groupName : TextView = itemView.findViewById(R.id.rw_group_name)
        private val groupCheckbox : CheckBox = itemView.findViewById(R.id.rw_group_checkbox)

        fun bind(item : TaskGroupItem) {
            groupColor.setColorFilter(Color.parseColor(item.group.color))
            groupName.text = item.group.name
            groupCheckbox.isChecked = item.isSelected
        }
    }

    var setGroupListener : SetTaskItemGroupListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupItemViewHolder {
        val holder = GroupItemViewHolder.create(parent)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val item = getItem(position)
            val previousSelected = currentList.find { it.isSelected }
            previousSelected?.isSelected = false
            item.isSelected = holder.itemView.isSelected
            setGroupListener?.onGroupSelected(previousSelected?.group, item.group)
        }
        return holder
    }

    override fun onBindViewHolder(holder: GroupItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

}

