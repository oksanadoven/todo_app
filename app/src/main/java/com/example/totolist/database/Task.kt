package com.example.totolist.database

import androidx.room.*

@Entity(
    tableName = "task_items",
)
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    @ColumnInfo(name = "content")
    var text: String,
    @ColumnInfo(name = "is_done")
    var isDone: Boolean,
    @ColumnInfo(name = "task_id")
    val taskId: Long = -1L
) : Comparable<TaskItem> {
    override fun compareTo(other: TaskItem): Int {
        return when {
            isDone && !other.isDone -> 1
            !isDone && other.isDone -> -1
            else -> 0
        }
    }
}

@Entity(
    tableName = "tasks",
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    @ColumnInfo(name = "title")
    var header: String,
    @ColumnInfo(name = "date")
    val date: Long
)

data class TaskWithItems(
    @Embedded
    val task: Task,
    @Relation(
        parentColumn = "id",
        entityColumn = "task_id"
    )
    var items: List<TaskItem>
)