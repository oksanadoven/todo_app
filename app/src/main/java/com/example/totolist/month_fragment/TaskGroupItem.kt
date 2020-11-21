package com.example.totolist.month_fragment

import com.example.totolist.R
import com.example.totolist.database.Group

class TaskGroupItem(
    val group: Group,
    var isSelected: Boolean = false
) : CalendarListItem {
    override fun getLayoutResId(): Int {
        return R.layout.group_item
    }

    override fun compareTo(other: CalendarListItem): Int {
        return 0
    }
}