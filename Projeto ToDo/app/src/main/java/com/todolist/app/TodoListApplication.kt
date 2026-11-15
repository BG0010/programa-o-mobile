package com.todolist.app

import android.app.Application
import android.content.Context
import com.todolist.app.data.local.AppDatabase
import com.todolist.app.data.preferences.SettingsRepository
import com.todolist.app.data.remote.BrasilApiService
import com.todolist.app.data.repository.AuthRepository
import com.todolist.app.data.repository.HolidayRepository
import com.todolist.app.data.repository.RoomTaskRepository
import com.todolist.app.data.repository.TaskRepository
import com.todolist.app.notifications.ReminderNotifications
import com.todolist.app.notifications.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Container simples de dependências (injeção manual), criado uma vez por processo. */
class AppContainer(context: Context) {
    val database = AppDatabase.create(context)
    val settingsRepository = SettingsRepository(context)
    val authRepository = AuthRepository(database, settingsRepository)
    val taskRepository: TaskRepository = RoomTaskRepository(database, settingsRepository)
    val holidayRepository = HolidayRepository(database.holidayDao(), BrasilApiService())
    val reminderScheduler = ReminderScheduler(context, settingsRepository, taskRepository)
}

class TodoListApplication : Application() {
    lateinit var container: AppContainer
        private set

    /** Escopo que vive enquanto o processo do app existir (para trabalhos fora das telas). */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderNotifications.createChannel(this)
        container.reminderScheduler.start(appScope)
    }
}
