package com.example.totolist.details

import android.os.Bundle
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.example.totolist.database.*

class TodoDetailsViewModel(
    dataSource: TasksDatabaseDao,
    taskId: Long,
    date: Long,
    private val savedState: SavedStateHandle
) : ViewModel() {

    companion object {
        const val TASK_WITH_ITEMS = "TASK_WITH_ITEMS"
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle?,
        private val dataSource: TasksDatabaseDao,
        private val taskId: Long,
        private val dateUTC: Long
    ) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            if (modelClass.isAssignableFrom(TodoDetailsViewModel::class.java)) {
                return TodoDetailsViewModel(dataSource, taskId, dateUTC, handle) as T
            }
            throw IllegalArgumentException("Unknown ViewModel Class")
        }
    }

    private val database = dataSource
    private val _taskWithItems = MediatorLiveData<TaskWithItems>().apply {
        if (savedState.contains(TASK_WITH_ITEMS)) {
            value = savedState.get(TASK_WITH_ITEMS)
        } else if (taskId == 0L) {
            value = TaskWithItems(
                task = Task(
                    header = "",
                    date = date
                ),
                items = listOf(emptyField()),
                group = null
            )
        } else {
            val databaseTask = database.getTaskWithItems(taskId)
            addSource(databaseTask) { task ->
                value = task.copy(items = task.items.plus(emptyField()))
                removeSource(databaseTask)
            }
        }
        observeForever { task ->
            savedState.set(TASK_WITH_ITEMS, task)
        }
    }
    val taskWithItems: LiveData<TaskWithItems> = _taskWithItems
    val group: LiveData<Group> = Transformations.switchMap(taskWithItems) {
        database.getGroupById(it.task.taskGroupId)
    }

    suspend fun save() {
        val current = _taskWithItems.value!!
        val items = current.items.filterNot { it.text.isEmpty() }
        database.insertOrUpdateTask(
            task = current.task,
            taskItems = items
        )
    }

    suspend fun deleteTaskWithItems(task: Task) {
        database.deleteTaskWithItems(task)
    }

    fun setHeaderText(text: String) {
        val current = _taskWithItems.value!!
        if (current.task.header == text) return
        _taskWithItems.value = current.copy(
            task = current.task.copy(
                header = text
            )
        )
    }

    fun setGroupId(id: Long) {
        val current = taskWithItems.value!!
        _taskWithItems.value = current.copy(
            task = current.task.copy(
                taskGroupId = id
            )
        )
    }

    private fun emptyField() = TaskItem(text = "", isDone = false)

    fun updateItem(item: TaskItem) {
        val current = taskWithItems.value!!
        val itemPosition = current.items.indexOf(item)
        val newItems = ArrayList(current.items)
        newItems[itemPosition] = (TaskItem(text = item.text, isDone = item.isDone))
        val sortedItems = newItems.sorted()
        var new = current.copy(items = sortedItems)
        if (itemPosition == current.items.lastIndex) {
            new = new.copy(items = new.items.plus(emptyField()))
        }
        _taskWithItems.value = new
    }
}