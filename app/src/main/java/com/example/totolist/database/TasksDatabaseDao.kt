package com.example.totolist.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TasksDatabaseDao {
    @Insert
    suspend fun insertTask(task: Task): Long

    @Insert
    suspend fun insertTaskItems(taskItemList: List<TaskItem>)

    @Insert
    fun insertGroup(group: Group): Long

    @Update
    suspend fun updateTask(task: Task)

    @Update
    suspend fun updateTaskItems(items: List<TaskItem>)

    @Update
    suspend fun updateGroup(group: Group)

    @Query("UPDATE task_items SET is_done = :isDone WHERE id = :itemId")
    suspend fun updateTaskItemById(itemId: Long, isDone: Boolean)

    @Query("UPDATE tasks SET task_group_id = :groupId WHERE id = :taskId")
    suspend fun updateGroupForTaskById(taskId: Long, groupId: Long)

    @Delete
    suspend fun deleteTask(task: Task)

    @Delete
    suspend fun deleteTaskItem(taskItem: TaskItem)

    @Delete
    suspend fun deleteGroup(group: Group)

    @Query("SELECT * from groups")
    fun getAllGroups(): LiveData<List<Group>>

    @Transaction
    @Query("SELECT * from groups WHERE groupId = :groupId LIMIT 1")
    fun getGroupById(groupId: Long) : LiveData<Group>

    @Transaction
    @Query("SELECT * from tasks")
    fun getAllTasksWithItems(): LiveData<List<TaskWithItems>>

    @Transaction
    @Query("SELECT * from tasks WHERE id = :taskId")
    fun getTaskWithItems(taskId: Long): LiveData<TaskWithItems>

    @Transaction
    @Query("SELECT * from tasks WHERE date = :date")
    suspend fun getTaskWithItemsByDate(date: Long): List<TaskWithItems>

    @Transaction
    @Query("SELECT * from tasks WHERE task_group_id = :groupId")
    suspend fun getTasksWithItemsByGroup(groupId: Long): List<TaskWithItems>

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
