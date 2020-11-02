package com.example.totolist.month_fragment

import com.example.totolist.R
import com.example.totolist.database.TaskItem

data class CalendarTaskCheckboxItem(
    val item: TaskItem
) : CalendarListItem {
    override fun getLayoutResId(): Int {
        return R.layout.calendar_task_checkbox_item
    }

    override fun compareTo(other: CalendarListItem): Int {
        if (other is CalendarTaskCheckboxItem) {
            if ((item.taskId == other.item.taskId) && (item.isDone && !(other.item.isDone))) {
                return 1
            }
            if ((item.taskId == other.item.taskId) && (!item.isDone && (other.item.isDone))) {
                return -1
            }
        }
        return 0
    }
}