package com.example.totolist.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Task::class, TaskItem::class], version = 1, exportSchema = false)
abstract class TasksDatabase: RoomDatabase() {
    abstract val tasksDatabaseDao: TasksDatabaseDao

    companion object {
        //The value of a volatile variable will never be cached, and all writes
        //and reads will be done to and from the main memory.
        //It means that changes made by one thread to INSTANCE
        //are visible to all other threads immediately
        @Volatile
        private var INSTANCE: TasksDatabase? = null
        fun getInstance(context: Context) : TasksDatabase {
            //only one thread of execution at a time can enter this block of code,
            // which makes sure the database only gets initialized once
            synchronized(this){
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TasksDatabase::class.java,
                        "tasks"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

    }
}