package com.example.totolist.goups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.totolist.database.Group
import com.example.totolist.database.TasksDatabaseDao
import com.example.totolist.month_fragment.TaskGroupItem
import kotlinx.coroutines.launch

class GroupsViewModel(dataSource: TasksDatabaseDao) : ViewModel() {

    val database = dataSource

    private fun getAllGroups(): List<Group> {
        var groups = listOf<Group>()
        viewModelScope.launch {
            groups = database.getAllGroups()
        }
        return groups
    }

    fun populateTaskGroupList(selectedGroup: Long) : List<TaskGroupItem> {
        val list = ArrayList<TaskGroupItem>()
        getAllGroups().forEach { group ->
            if (group.groupId == selectedGroup) {
                list.add(TaskGroupItem(group, true))
            } else {
                list.add(TaskGroupItem(group, false))
            }
        }
        return list
    }

    fun addNewGroup(group: Group) {
        viewModelScope.launch {
            database.insertGroup(group)
        }
    }

    suspend fun updateGroup(group: Group) {
        database.updateGroup(group)
    }
}