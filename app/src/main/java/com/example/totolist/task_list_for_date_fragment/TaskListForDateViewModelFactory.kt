package com.example.totolist.task_list_for_date_fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.totolist.database.TasksDatabaseDao

class TaskListForDateViewModelFactory(
    private val dataSource: TasksDatabaseDao,
) : ViewModelProvider.Factory {

    @Suppress("unchecked cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskListForDateViewModel::class.java)) {
            return TaskListForDateViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}
