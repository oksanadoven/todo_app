package com.example.totolist.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.totolist.database.TasksDatabaseDao

class TodoDetailsViewModelFactory(
    private val dataSource: TasksDatabaseDao,
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoDetailsViewModel::class.java)) {
            return TodoDetailsViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}