package com.example.totolist.search_fragment

import androidx.lifecycle.*
import com.example.totolist.database.TaskWithItems
import com.example.totolist.database.TasksDatabaseDao
import com.example.totolist.month_fragment.CalendarListItem
import com.example.totolist.month_fragment.CalendarTaskCheckboxItem
import com.example.totolist.month_fragment.CalendarTaskHeaderItem
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter

class SearchViewModel(
    dataSource: TasksDatabaseDao,
) : ViewModel() {

    private val database = dataSource
    private val _searchItemsLiveData: LiveData<List<SearchItem>> =
        Transformations.map(database.getAllTasksWithItems()) { tasks ->
            tasks.map { it.copy(items = it.items.sorted()) }
                .groupBy { taskItem -> taskItem.toGroupKey() }
                .flatMap { group ->
                    val date = keyToMillis(group.key)
                    val itemsForDate = group.value
                    listOf(SearchHeaderItem(date))
                        .plus(itemsForDate.map { taskItem ->
                            SearchListItem(
                                taskItem.task,
                                children = ArrayList<CalendarListItem>()
                                    .plus(CalendarTaskHeaderItem(task = taskItem.task))
                                    .plus(taskItem.items.map { CalendarTaskCheckboxItem(it) })
                            )
                        })
                }
        }

    private val queryLiveData = MutableLiveData<String>().apply { value = "" }

    val taskItemsLiveData: LiveData<List<SearchItem>> =
        MediatorLiveData<List<SearchItem>>().apply {
            addSource(queryLiveData) {
                value = calculateList(
                    query = queryLiveData.value,
                    originalList = _searchItemsLiveData.value ?: emptyList()
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
        originalList: List<SearchItem>
    ): List<SearchItem> {
        if (query.isNullOrEmpty()) {
            return originalList
        }
        return originalList.filter { item ->
            if (item is SearchListItem) {
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
            }
            return@filter false
        }
    }
}

private fun TaskWithItems.toGroupKey(): String {
    return Instant
        .ofEpochMilli(task.date)
        .atZone(ZoneId.of("UTC"))
        .format(DateTimeFormatter.ISO_WEEK_DATE)
}

private fun keyToMillis(date: String): Long {
    return LocalDate
        .parse(date, DateTimeFormatter.ISO_WEEK_DATE)
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()
}
