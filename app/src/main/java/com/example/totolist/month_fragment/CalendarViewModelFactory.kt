package com.example.totolist.month_fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.totolist.database.TasksDatabaseDao

class CalendarViewModelFactory (
    dataSource: TasksDatabaseDao
): ViewModelProvider.Factory {
    private val database = dataSource

    @Suppress ("unchecked cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            return CalendarViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}