package com.example.totolist.goups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.totolist.database.TasksDatabaseDao

class GroupsViewModelFactory (
    private val dataSource: TasksDatabaseDao,
): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupsViewModel::class.java)) {
            return GroupsViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}