package com.todolist.app.notifications

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.todolist.app.data.preferences.SettingsRepository
import com.todolist.app.data.repository.TaskRepository
import com.todolist.app.domain.Reminders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

/**
 * Mantém os lembretes agendados no WorkManager sincronizados com as tarefas do usuário logado.
 * Sempre que as tarefas, a sessão ou a preferência mudam, todos os lembretes são refeitos:
 * simples e suficiente para a quantidade de tarefas de um usuário.
 */
class ReminderScheduler(
    context: Context,
    private val settings: SettingsRepository,
    private val tasks: TaskRepository,
) {
    private val workManager = WorkManager.getInstance(context)

    @OptIn(FlowPreview::class)
    fun start(scope: CoroutineScope) = scope.launch {
        combine(settings.settings, settings.sessionUserId, tasks.tasks) { s, userId, list -> Triple(s.notifications, userId, list) }
            .debounce(500)
            .collect { (enabled, userId, list) ->
                workManager.cancelAllWorkByTag(TAG)
                if (!enabled || userId == null) return@collect
                val now = LocalDateTime.now()
                Reminders.upcoming(list, now).forEach { (task, at) ->
                    val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                        .setInitialDelay(Duration.between(now, at))
                        .setInputData(workDataOf(ReminderWorker.KEY_TASK_ID to task.id, ReminderWorker.KEY_USER_ID to userId))
                        .addTag(TAG)
                        .build()
                    workManager.enqueue(request)
                }
            }
    }

    private companion object {
        const val TAG = "task-reminder"
    }
}
