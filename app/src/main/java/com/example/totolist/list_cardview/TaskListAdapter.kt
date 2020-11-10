package com.example.totolist.list_cardview



/*
class TaskListAdapter : ListAdapter<TaskListItem, TaskListAdapter.TaskViewHolder>(
    TasksDiffCallback()
) {

    interface OnItemClickListener {
        fun onItemSelected(item: TaskListItem)
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listHeader: TextView = itemView.list_header
    }

    private class TasksDiffCallback : DiffUtil.ItemCallback<TaskListItem>() {

        override fun areItemsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean {
            return oldItem.taskWithItems.task.id == newItem.taskWithItems.task.id
        }

        override fun areContentsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean {
            return oldItem == newItem
        }
    }

    var onItemClickListener: OnItemClickListener? = null
    private var checkedItems = MutableLiveData<List<TaskListItem>>().apply { value = emptyList() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.search_card_item, parent, false)
        val holder = TaskViewHolder(itemView)
        itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = getItem(position)
                if (isSelectModeOn()) {
                    item.switchSelectionMode()
                    notifyItemChanged(position)
                } else {
                    if (!item.isChecked) {
                        onItemClickListener?.onItemSelected(item)
                    }
                }
            }
        }
        holder.itemView.setOnLongClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = getItem(position)
                item.switchSelectionMode()
                notifyItemChanged(position)
            }
            return@setOnLongClickListener true
        }
        return holder
    }

    private fun TaskListItem.switchSelectionMode() {
        isChecked = !isChecked
        val items = checkedItems.value ?: emptyList()
        if (isChecked) {
            checkedItems.value = items.plus(this)
        } else {
            checkedItems.value = items.minus(this)
        }
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.listHeader.text = currentItem.taskWithItems.task.header
        bindTaskItems(holder, currentItem)
        holder.itemView.setBackgroundColor(
            if (currentItem.isChecked) holder.itemView.resources.getColor(R.color.colorCheckedItem)
            else Color.WHITE
        )
    }

    private fun bindTaskItems(
        holder: TaskViewHolder,
        currentItem: TaskListItem
    ) {
*/
/*        hideAllViews(holder)
        val taskItemsList = currentItem.taskWithItems.items
        var i = 0
        if (taskItemsList.isNotEmpty()) {
            while (i < 5 && i != taskItemsList.size) {
                when (i) {
                    0 -> setTaskItemStyle(holder.task1, holder.taskCheckBox1, currentItem, i)
                    1 -> setTaskItemStyle(holder.task2, holder.taskCheckBox2, currentItem, i)
                    2 -> setTaskItemStyle(holder.task3, holder.taskCheckBox3, currentItem, i)
                    3 -> setTaskItemStyle(holder.task4, holder.taskCheckBox4, currentItem, i)
                    4 -> setTaskItemStyle(holder.task5, holder.taskCheckBox5, currentItem, i)
                }
                i++
            }
        }
    }*//*


    private fun setTaskItemStyle(
        taskText: TextView,
        checkbox: CheckBox,
        currentItem: TaskListItem,
        position: Int
    ) {
        taskText.text = currentItem.taskWithItems.items[position].text
        taskText.isVisible = true
        checkbox.isChecked = currentItem.taskWithItems.items[position].isDone
        if (checkbox.isChecked) {
            taskText.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            taskText.paintFlags = 0
        }
        checkbox.isVisible = true
    }

    private fun hideAllViews(holder: TaskViewHolder) {
        holder.task1.visibility = View.GONE
        holder.taskCheckBox1.visibility = View.GONE
        holder.task2.visibility = View.GONE
        holder.taskCheckBox2.visibility = View.GONE
        holder.task3.visibility = View.GONE
        holder.taskCheckBox3.visibility = View.GONE
        holder.task4.visibility = View.GONE
        holder.taskCheckBox4.visibility = View.GONE
        holder.task5.visibility = View.GONE
        holder.taskCheckBox5.visibility = View.GONE
    }

    fun setTasks(newTasks: List<TaskListItem>) {
        submitList(newTasks)
    }

    fun getCheckedItems(): LiveData<List<TaskListItem>> {
        return checkedItems
    }

    private fun clearCheckedItems() {
        checkedItems.value = emptyList()
    }

    private fun isSelectModeOn(): Boolean {
        return checkedItems.value!!.isNotEmpty()
    }

    fun selectModeOff() {
        if (isSelectModeOn()) {
            currentList.forEachIndexed { index, taskListItem ->
                if (taskListItem.isChecked) {
                    taskListItem.isChecked = false
                    notifyItemChanged(index)
                }
            }
            clearCheckedItems()
        }
    }
}
*/

