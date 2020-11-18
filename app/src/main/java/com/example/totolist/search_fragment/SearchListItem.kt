package com.example.totolist.search_fragment

import com.example.totolist.R
import com.example.totolist.database.Task
import com.example.totolist.month_fragment.CalendarListItem

data class SearchListItem (
    val task: Task,
    val children: List<CalendarListItem>
) : SearchItem {

    override fun getLayoutResId(): Int {
        return R.layout.search_card_item
    }
}