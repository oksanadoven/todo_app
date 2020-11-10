package com.example.totolist.month_fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.totolist.database.TasksDatabaseDao
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class CalendarViewModel(dataSource: TasksDatabaseDao) : ViewModel() {

    private val database = dataSource
    val dateLiveData = MutableLiveData<Long>().apply {
        value = ZonedDateTime.of(LocalDateTime.now().toLocalDate().atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun setDate(date: Long) {
        dateLiveData.value = date
    }

    private suspend fun getCalendarItemsByDate(date: Long): List<CalendarListItem> {
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

    suspend fun calculateTotalTasksForDay(date: Long): Int {
        var taskItemCount = 0
        val items = getCalendarItemsByDate(date)
        for (item in items) {
            if (item is CalendarTaskCheckboxItem) {
                if (!item.item.isDone) taskItemCount++
            }
        }
        return taskItemCount
    }
}