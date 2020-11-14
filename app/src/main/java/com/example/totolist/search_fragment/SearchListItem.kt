package com.example.totolist.search_fragment

import com.example.totolist.database.Task
import com.example.totolist.month_fragment.CalendarListItem

data class SearchListItem(
    val task: Task,
    val children: List<CalendarListItem>
)