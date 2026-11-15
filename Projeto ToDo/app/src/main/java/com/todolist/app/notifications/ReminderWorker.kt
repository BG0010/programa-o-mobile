package com.todolist.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.todolist.app.TodoListApplication
import com.todolist.app.util.label
import com.todolist.app.util.relativeDueLabel
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Executado pelo WorkManager no horário do lembrete.
 * Antes de notificar, confere de novo no banco: a tarefa pode ter sido concluída,
 * excluída ou pertencer a outro usuário que não está mais logado.
 */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(KEY_TASK_ID, -1)
        val userId = inputData.getLong(KEY_USER_ID, -1)
        val container = (applicationContext as TodoListApplication).container

        val settings = container.settingsRepository.settings.first()
        val sessionUser = container.settingsRepository.sessionUserId.first()
        if (!settings.notifications || sessionUser != userId) return Result.success()

        val task = container.database.taskDao().find(taskId, userId) ?: return Result.success()
        if (task.isDone) return Result.success()

        val whenText = relativeDueLabel(task.dueDate, LocalDate.now()) + (task.dueTime?.let { " às ${it.label()}" } ?: "")
        ReminderNotifications.show(applicationContext, taskId, task.title, whenText)
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "taskId"
        const val KEY_USER_ID = "userId"
    }
}
