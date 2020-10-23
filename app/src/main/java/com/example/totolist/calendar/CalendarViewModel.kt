package com.example.totolist.calendar

import android.app.Application
import androidx.lifecycle.*
import com.example.totolist.TaskListItem
import com.example.totolist.database.TasksDatabaseDao

class CalendarViewModel(
    dataSource: TasksDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    private val database = dataSource

    private val _taskItemsLiveData: LiveData<List<TaskListItem>> =
        Transformations.map(database.getAllTasksWithItems()) { tasks ->
            tasks.map { it.copy(items = it.items.sorted()) }
                .map { task -> TaskListItem(task, false) }
        }

    private val queryLiveData = MutableLiveData<String>()

    val taskItemsLiveData: LiveData<List<TaskListItem>> =
        MediatorLiveData<List<TaskListItem>>().apply {
            addSource(queryLiveData) {
                value = calculateList(queryLiveData.value, _taskItemsLiveData.value!!)
            }
            addSource(_taskItemsLiveData) {
                value = calculateList(queryLiveData.value, _taskItemsLiveData.value!!)
            }
        }

    fun setQuery(text: String) {
        queryLiveData.value = text
    }

    private fun calculateList(
        query: String?,
        originalList: List<TaskListItem>
    ): List<TaskListItem> {
        if (query.isNullOrEmpty()) {
            return originalList
        }
        return originalList.filter { item ->
            item.taskWithItems.task.header.contains(query, true)
        }
    }

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