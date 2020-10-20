package com.example.totolist.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.totolist.TasksDatabaseDao

class CalendarViewModel(
    dataSource: TasksDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    private val database = dataSource

    suspend fun getCalendarItemsByDate(date: String): List<CalendarListItem> {
        return database.getTaskWithItemsByDate(date).flatMap { taskWithItems ->
            val items = ArrayList<CalendarListItem>()
            items.add(CalendarTaskHeaderItem(taskWithItems.task))
            val taskItems = taskWithItems.items.map { taskItem ->
                CalendarTaskCheckboxItem(taskItem)
            }
            items.addAll(taskItems)
            items
        }
    }

    suspend fun updateTaskItems(itemId: Long, isDone: Boolean) {
        database.updateTaskItemById(itemId, isDone)
    }
}