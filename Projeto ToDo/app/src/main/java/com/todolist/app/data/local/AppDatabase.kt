package com.todolist.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [UserEntity::class, CategoryEntity::class, TaskEntity::class, SubtaskEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "todolist.db").build()
    }
}
