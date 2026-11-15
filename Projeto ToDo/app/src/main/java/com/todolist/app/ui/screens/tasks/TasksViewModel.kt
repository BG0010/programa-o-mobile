package com.todolist.app.ui.screens.tasks

import androidx.lifecycle.viewModelScope
import com.todolist.app.data.preferences.SettingsRepository
import com.todolist.app.data.repository.TaskRepository
import com.todolist.app.domain.Reminders
import com.todolist.app.domain.model.Category
import com.todolist.app.domain.model.Task
import com.todolist.app.domain.model.Urgency
import com.todolist.app.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalDateTime

enum class TaskFilter(val label: String, val sectionTitle: String) {
    ALL("Todas", "Próximas tarefas"),
    URGENT("Urgentes", "Urgentes"),
    TODAY("Hoje", "Hoje"),
    DONE("Concluídas", "Concluídas"),
}

data class TasksUiState(
    val isLoading: Boolean = true,
    /** Total de tarefas do usuário, sem filtros (para diferenciar "sem tarefas" de "filtro vazio"). */
    val totalCount: Int = 0,
    val today: LocalDate = LocalDate.now(),
    val query: String = "",
    val filter: TaskFilter = TaskFilter.ALL,
    val tasks: List<Task> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val urgentCount: Int = 0,
    val todayTotal: Int = 0,
    val todayDone: Int = 0,
    val notificationsEnabled: Boolean = false,
    /** Próximos lembretes (tarefa + horário do aviso). */
    val upcomingReminders: List<Pair<Task, LocalDateTime>> = emptyList(),
) {
    val todayPending: Int get() = todayTotal - todayDone
    val todayProgress: Float get() = if (todayTotal == 0) 0f else todayDone.toFloat() / todayTotal
}

class TasksViewModel(
    private val repository: TaskRepository,
    settings: SettingsRepository,
) : BaseViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(TaskFilter.ALL)

    private val baseState = combine(
        repository.tasks, repository.categories, query, filter,
    ) { tasks, categories, query, filter ->
        val today = LocalDate.now()
        val isUrgent = { t: Task -> !t.isDone && t.urgency(today) in UrgentSet }
        val todayTasks = tasks.filter { it.dueDate == today }

        val filtered = tasks
            .filter {
                when (filter) {
                    TaskFilter.ALL -> true
                    TaskFilter.URGENT -> isUrgent(it)
                    TaskFilter.TODAY -> it.dueDate == today
                    TaskFilter.DONE -> it.isDone
                }
            }
            .filter { query.isBlank() || it.title.contains(query.trim(), ignoreCase = true) }
            // Ordenação por prioridade: pendentes primeiro, depois selo, prazo e horário.
            .sortedWith(
                compareBy<Task>({ it.isDone }, { it.urgency(today).ordinal }, { it.dueDate }, { it.dueTime }),
            )

        TasksUiState(
            isLoading = false,
            totalCount = tasks.size,
            today = today,
            query = query,
            filter = filter,
            tasks = filtered,
            categories = categories.associateBy { it.id },
            urgentCount = tasks.count(isUrgent),
            todayTotal = todayTasks.size,
            todayDone = todayTasks.count { it.isDone },
            upcomingReminders = Reminders.upcoming(tasks, LocalDateTime.now()).take(5),
        )
    }

    val uiState: StateFlow<TasksUiState> = combine(baseState, settings.settings) { state, s ->
        state.copy(notificationsEnabled = s.notifications)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())

    fun onQueryChange(value: String) { query.value = value }
    fun onFilterChange(value: TaskFilter) { filter.value = value }

    fun setDone(task: Task, done: Boolean) = launchSafely(
        errorMessage = "Não foi possível atualizar a tarefa.",
        successMessage = if (done) "Tarefa concluída" else "Tarefa reaberta",
    ) { repository.setTaskDone(task.id, done) }

    fun delete(task: Task) = launchSafely(
        errorMessage = "Não foi possível excluir a tarefa.",
        successMessage = "Tarefa excluída",
    ) { repository.deleteTask(task.id) }

    private companion object {
        val UrgentSet = setOf(Urgency.OVERDUE, Urgency.URGENT)
    }
}
