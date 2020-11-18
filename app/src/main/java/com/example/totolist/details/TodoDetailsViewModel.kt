package com.example.totolist.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.totolist.database.Task
import com.example.totolist.database.TaskItem
import com.example.totolist.database.TaskWithItems
import com.example.totolist.database.TasksDatabaseDao

class TodoDetailsViewModel(
    dataSource: TasksDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

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