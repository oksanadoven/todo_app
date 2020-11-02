package com.example.totolist

import com.example.totolist.database.TaskWithItems

data class TaskListItem(
    var taskWithItems: TaskWithItems,
    var isChecked: Boolean
)