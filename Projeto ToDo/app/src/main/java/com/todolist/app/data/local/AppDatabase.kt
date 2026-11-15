package com.todolist.app.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [UserEntity::class, CategoryEntity::class, TaskEntity::class, SubtaskEntity::class, HolidayEntity::class],
    version = 2,
    exportSchema = true,
    // v2 (Etapa 5): nova tabela "holidays". A migração automática preserva os dados existentes.
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao
    abstract fun holidayDao(): HolidayDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "todolist.db").build()
    }
}
