package com.example.totolist.details

import androidx.lifecycle.ViewModel
import com.example.totolist.database.Task
import com.example.totolist.database.TaskItem
import com.example.totolist.database.TaskWithItems
import com.example.totolist.database.TasksDatabaseDao

class TodoDetailsViewModel(dataSource: TasksDatabaseDao, ) : ViewModel() {

    private val database = dataSource

    suspend fun insert(task: Task): Long {
        return database.insertTask(task)
    }

    suspend fun getTaskWithItems(taskId: Long): TaskWithItems {
        return database.getTaskWithItems(taskId)
    }

    suspend fun deleteAndInsert(task: Task, taskItems: List<TaskItem>) {
        database.deleteAndInsert(task, taskItems, listOf(task.id))
    }

    suspend fun deleteTaskWithItems(task: Task) {
        database.deleteTaskWithItems(task)
    }
}