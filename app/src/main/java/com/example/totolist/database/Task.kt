package com.example.totolist.database

import androidx.room.*
import java.io.Serializable

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
) : Comparable<TaskItem>, Serializable {
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
    val date: Long,
    @ColumnInfo(name = "task_group_id")
    val taskGroupId: Long = 0L
)

@Entity(
    tableName = "groups"
)
data class Group(
    @PrimaryKey(autoGenerate = true)
    var groupId: Long = 0L,
    @ColumnInfo(name = "title")
    var name: String,
    @ColumnInfo(name = "color")
    var color: String
)

data class TaskWithItems (
    @Embedded
    val task: Task,
    @Relation(
        parentColumn = "id",
        entityColumn = "task_id"
    )
    var items: List<TaskItem>,
    @Relation(
        parentColumn = "task_group_id",
        entityColumn = "groupId"
    )
    var group: Group?
) : Serializable