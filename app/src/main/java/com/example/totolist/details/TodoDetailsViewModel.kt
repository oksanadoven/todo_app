package com.example.totolist.details

import androidx.lifecycle.*
import com.example.totolist.database.*

class TodoDetailsViewModel(dataSource: TasksDatabaseDao) : ViewModel() {

    class Factory(
        private val dataSource: TasksDatabaseDao
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TodoDetailsViewModel::class.java)) {
                return TodoDetailsViewModel(dataSource) as T
            }
            throw IllegalArgumentException("Unknown ViewModel Class")
        }
    }

    private val database = dataSource
    private val taskId = MutableLiveData<Long>()
    val taskWithItems: LiveData<TaskWithItems> = Transformations.switchMap(taskId) { id ->
        if (id == 0L) {
            createDefaultLiveData()
        } else {
            database.getTaskWithItems(id)
        }
    }
    private val groupId = MediatorLiveData<Long>().apply {
        addSource(taskWithItems) { task ->
            val group = task.group
            if (value == null && group != null) {
                value = group.groupId
            }
        }
    }
    private val date = MutableLiveData<Long>()
    val group: LiveData<Group> = Transformations.switchMap(groupId) { id ->
        database.getGroupById(id)
    }

    private fun createDefaultLiveData(): LiveData<TaskWithItems> {
        return MutableLiveData<TaskWithItems>().apply {
            value = TaskWithItems(
                Task(
                    header = "",
                    date = date.value!!
                ), emptyList(), group = null
            )
        }
    }

    suspend fun insert(task: Task): Long {
        return database.insertTask(task)
    }

    suspend fun deleteAndInsert(task: Task, taskItems: List<TaskItem>) {
        database.deleteAndInsert(task, taskItems, listOf(task.id))
    }

    suspend fun deleteTaskWithItems(task: Task) {
        database.deleteTaskWithItems(task)
    }

    fun setGroupId(id: Long) {
        groupId.postValue(id)
    }

    fun setTaskId(id: Long) {
        taskId.postValue(id)
    }

    fun setDate(dateInMs: Long) {
        date.value = dateInMs
    }
}