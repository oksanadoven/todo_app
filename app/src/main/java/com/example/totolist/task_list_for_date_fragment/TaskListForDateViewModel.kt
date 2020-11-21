package com.example.totolist.task_list_for_date_fragment

import androidx.lifecycle.ViewModel
import com.example.totolist.database.Task
import com.example.totolist.database.TasksDatabaseDao
import com.example.totolist.month_fragment.CalendarListItem
import com.example.totolist.month_fragment.CalendarTaskCheckboxItem
import com.example.totolist.month_fragment.CalendarTaskHeaderItem
import com.example.totolist.month_fragment.TaskGroupItem

class TaskListForDateViewModel(dataSource: TasksDatabaseDao) : ViewModel() {

    private val database = dataSource

    suspend fun getCalendarItemsByDate(date: Long): List<CalendarListItem> {
        return database.getTaskWithItemsByDate(date).flatMap { taskWithItems ->
            val items = ArrayList<CalendarListItem>()
            items.add(CalendarTaskHeaderItem(taskWithItems.task))
            val taskItems = taskWithItems.items.map { taskItem ->
                CalendarTaskCheckboxItem(taskItem)
            }
            items.addAll(taskItems)
            val group = if (taskWithItems.task.taskGroupId != 0L) {
                taskWithItems.group?.let { TaskGroupItem(it) }
            } else null
            if (group != null) {
                items.add(group)
            }
            items
        }
    }

    suspend fun updateTaskItems(itemId: Long, isDone: Boolean) {
        database.updateTaskItemById(itemId, isDone)
    }

    suspend fun deleteTask(task: Task) {
        database.deleteTaskWithItems(task)
    }
}