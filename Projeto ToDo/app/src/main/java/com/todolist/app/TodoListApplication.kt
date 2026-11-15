package com.todolist.app

import android.app.Application
import android.content.Context
import com.todolist.app.data.local.AppDatabase
import com.todolist.app.data.preferences.SettingsRepository
import com.todolist.app.data.repository.AuthRepository
import com.todolist.app.data.repository.RoomTaskRepository
import com.todolist.app.data.repository.TaskRepository

/** Container simples de dependências (injeção manual), criado uma vez por processo. */
class AppContainer(context: Context) {
    private val database = AppDatabase.create(context)
    val settingsRepository = SettingsRepository(context)
    val authRepository = AuthRepository(database, settingsRepository)
    val taskRepository: TaskRepository = RoomTaskRepository(database, settingsRepository)
}

class TodoListApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
