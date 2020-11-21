package com.example.totolist.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.totolist.utils.ioThread

@Database(
    entities = [Task::class, TaskItem::class, Group::class],
    version = 1,
    exportSchema = false
)
abstract class TasksDatabase : RoomDatabase() {
    abstract fun taskDBDao(): TasksDatabaseDao

    companion object {
        //The value of a volatile variable will never be cached, and all writes
        //and reads will be done to and from the main memory.
        //It means that changes made by one thread to INSTANCE
        //are visible to all other threads immediately
        @Volatile
        private var INSTANCE: TasksDatabase? = null
        fun getInstance(context: Context): TasksDatabase =
        //only one thread of execution at a time can enter this block of code,
            // which makes sure the database only gets initialized once
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDataBase(context).also { INSTANCE = it }
            }

        private fun buildDataBase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                TasksDatabase::class.java,
                "tasks"
            )
                //Prepopulate the Db with "No group" Group
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        ioThread {
                            getInstance(context).taskDBDao().insertGroup(
                                Group(
                                    name = "No Group",
                                    color = "#757575",
                                    groupId = 0L
                                )
                            )
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
    }
}
