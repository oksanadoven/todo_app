package com.example.totolist.search_fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.totolist.database.TasksDatabaseDao

class SearchViewModelFactory(dataSource: TasksDatabaseDao): ViewModelProvider.Factory {
    private val database = dataSource

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}