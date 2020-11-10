package com.example.totolist.search_fragment

import androidx.lifecycle.*
import com.example.totolist.database.TasksDatabaseDao
import com.example.totolist.month_fragment.CalendarListItem
import com.example.totolist.month_fragment.CalendarTaskCheckboxItem
import com.example.totolist.month_fragment.CalendarTaskHeaderItem

class SearchViewModel(
    dataSource: TasksDatabaseDao,
) : ViewModel() {

    private val database = dataSource
    private val _searchItemsLiveData: LiveData<List<SearchListItem>> =
        Transformations.map(database.getAllTasksWithItems()) { tasks ->
            tasks.map { it.copy(items = it.items.sorted()) }
                .map { taskItem ->
                    SearchListItem(
                        children = ArrayList<CalendarListItem>()
                            .plus(CalendarTaskHeaderItem(task = taskItem.task))
                            .plus(taskItem.items.map { CalendarTaskCheckboxItem(it) })
                    )
                }
        }

    private val queryLiveData = MutableLiveData<String>().apply { value = "" }

    val taskItemsLiveData: LiveData<List<SearchListItem>> =
        MediatorLiveData<List<SearchListItem>>().apply {
            addSource(queryLiveData) {
                value = calculateList(
                    query = queryLiveData.value,
                    originalList = _searchItemsLiveData.value?: emptyList()
                )
            }
            addSource(_searchItemsLiveData) {
                value = calculateList(
                    query = queryLiveData.value,
                    originalList = _searchItemsLiveData.value ?: emptyList()
                )
            }
        }

    fun setQuery(text: String) {
        queryLiveData.value = text
    }

    private fun calculateList(
        query: String?,
        originalList: List<SearchListItem>
    ): List<SearchListItem> {
        if (query.isNullOrEmpty()) {
            return originalList
        }
        return originalList.filter { item ->
            item.children.forEach {
                if (it is CalendarTaskHeaderItem) {
                    if (it.task.header.contains(query, true)) {
                        return@filter true
                    }
                } else if (it is CalendarTaskCheckboxItem) {
                    if (it.item.text.contains(query, true)) {
                        return@filter true
                    }
                }
            }
            return@filter false
        }
    }
}