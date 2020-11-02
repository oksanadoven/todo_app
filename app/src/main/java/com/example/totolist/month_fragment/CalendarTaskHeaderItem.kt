package com.example.totolist.month_fragment

import com.example.totolist.R
import com.example.totolist.database.Task

class CalendarTaskHeaderItem(
    val task: Task
) : CalendarListItem {
    override fun getLayoutResId(): Int {
        return R.layout.calendar_task_header_item
    }

    override fun compareTo(other: CalendarListItem): Int {
        return 0
    }
}