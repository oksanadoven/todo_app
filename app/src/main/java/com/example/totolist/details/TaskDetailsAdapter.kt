package com.example.totolist.details

import android.graphics.Paint
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.totolist.R
import com.example.totolist.database.TaskItem
import kotlinx.android.synthetic.main.task_list_details_checkboxes.view.*

class TaskDetailsAdapter : ListAdapter<TaskItem, TaskDetailsAdapter.TaskDetailsViewHolder>(
    TasksDiffCallback()
) {

    class TaskDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskCheckbox: CheckBox = itemView.task_checkbox
        val taskText: EditText = itemView.edit_text_tasks
        val deleteItem: ImageView = itemView.delete_item
    }

    private class TasksDiffCallback : DiffUtil.ItemCallback<TaskItem>() {
        override fun areItemsTheSame(oldItem: TaskItem, newItem: TaskItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TaskItem, newItem: TaskItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskDetailsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.task_list_details_checkboxes, parent, false)
        val holder = TaskDetailsViewHolder(itemView)
        holder.taskText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    addEmptyTaskItem()
                }
            }
            false
        }
        holder.deleteItem.setOnClickListener {
            deleteTaskItem(holder.adapterPosition)
        }
        holder.taskText.doOnTextChanged { text, _, _, _ ->
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = getItem(position)
                item.text = text.toString()
            }
        }
        holder.taskCheckbox.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            compoundButton.isChecked = b
            val position = holder.adapterPosition
            val item = getItem(position)
            item.isDone = compoundButton.isChecked
            if (item.isDone) {
                holder.taskText.setTextColor(holder.taskText.context.resources.getColor(R.color.colorTextSecondary))
                holder.taskText.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                holder.taskText.setTextColor(holder.taskText.context.resources.getColor(R.color.colorTextPrimary))
                holder.taskText.paintFlags = 0
            }
            submitList(currentList.sorted())
        }
        return holder
    }

    fun addEmptyTaskItem() {
        val newItemsList = currentList.plus(TaskItem(text = "", isDone = false))
        submitList(newItemsList)
    }

    private fun deleteTaskItem(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val newItemsList = currentList.minus(getItem(position))
            submitList(newItemsList)
        }
    }

    override fun onBindViewHolder(holder: TaskDetailsViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.taskCheckbox.isChecked = currentItem.isDone
        holder.taskText.setText(currentItem.text, TextView.BufferType.EDITABLE)
    }
}