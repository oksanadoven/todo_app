package com.example.totolist.search_fragment

import android.app.Application
import androidx.lifecycle.*
import com.example.totolist.TaskListItem
import com.example.totolist.database.TasksDatabaseDao

class SearchFragmentViewModel (
    dataSource: TasksDatabaseDao,
    application: Application
): AndroidViewModel(application) {

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
}