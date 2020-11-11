package com.example.totolist.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TasksDatabaseDao {
    @Insert
    suspend fun insertTask(task: Task): Long

    @Insert
    suspend fun insertTaskItems(taskItemList: List<TaskItem>)

    @Update
    suspend fun updateTask(task: Task)

    @Update
    suspend fun updateTaskItems(items: List<TaskItem>)

    @Query("UPDATE task_items SET is_done = :isDone WHERE id = :itemId")
    suspend fun updateTaskItemById(itemId: Long, isDone: Boolean)

    @Delete
    suspend fun deleteTask(task: Task)

    @Delete
    suspend fun deleteTaskItem(taskItem: TaskItem)

    @Query("SELECT * from tasks")
    fun getAllTasksWithItems(): LiveData<List<TaskWithItems>>

    @Query("SELECT * from tasks WHERE id = :taskId")
    suspend fun getTaskWithItems(taskId: Long): TaskWithItems

    @Query("SELECT * from tasks WHERE date = :date")
    suspend fun getTaskWithItemsByDate(date: Long): List<TaskWithItems>

/*
    @Query("SELECT * from tasks ORDER BY id DESC LIMIT 1")
    suspend fun getLastItemAdded() : Task
*/

    @Transaction
    suspend fun deleteTaskWithItems(task: Task){
        batchDeleteItems(listOf(task.id))
        deleteTask(task)
    }

    @Transaction
    @Query("DELETE FROM tasks WHERE id IN (:idList)")
    suspend fun batchDeleteTasks(idList: List<Long>)

    @Transaction
    @Query("DELETE FROM task_items WHERE task_id IN (:idList)")
    suspend fun batchDeleteItems(idList: List<Long>)

    @Transaction
    suspend fun deleteAndInsert(task: Task, taskItems: List<TaskItem>, idList: List<Long>) {
        batchDeleteItems(idList)
        insertTaskItems(taskItems)
        updateTask(task)
    }
}
