package com.example.totolist.goups

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.totolist.database.Group
import com.example.totolist.database.TasksDatabaseDao
import com.example.totolist.month_fragment.TaskGroupItem
import com.example.totolist.utils.ioThread

class GroupsViewModel(dataSource: TasksDatabaseDao, selectedGroupId: Long) : ViewModel() {


    class Factory (
        private val dataSource: TasksDatabaseDao,
        private val selectedGroupId: Long
    ): ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GroupsViewModel::class.java)) {
                return GroupsViewModel(dataSource, selectedGroupId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel Class")
        }
    }
    val database = dataSource

    val groupsLiveData: LiveData<List<TaskGroupItem>> =
        Transformations.map(database.getAllGroups()) { groups ->
            groups.map { item->
                if (item.groupId == selectedGroupId) {
                    TaskGroupItem(item, true)
                } else {
                    TaskGroupItem(item, false)
                }
            }
        }

    fun addNewGroup(group: Group) {
        ioThread {
            database.insertGroup(group)
        }
    }

    fun setSelectedTask(oldGroup: Group, newGroup: Group, taskId: Long) {
        groupsLiveData.value?.find {
            if (it.group.groupId == oldGroup.groupId) {
                it.isSelected = false
            }
            if (it.group.groupId == newGroup.groupId) {
                it.isSelected = true
            }
            false
        }
    }

    suspend fun updateGroupForTask(selectedGroupId: Long, taskId: Long?) {
        database.updateGroupForTaskById(selectedGroupId, taskId!!)
    }

}