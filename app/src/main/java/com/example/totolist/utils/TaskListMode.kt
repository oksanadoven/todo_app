package com.example.totolist.utils

sealed class TaskListMode {
    object Normal : TaskListMode()
    data class Select(
        val selectedItemsCount: Int
    ) : TaskListMode()

    object Search: TaskListMode()
}