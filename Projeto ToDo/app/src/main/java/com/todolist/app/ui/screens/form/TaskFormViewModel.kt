package com.todolist.app.ui.screens.form

import androidx.lifecycle.viewModelScope
import com.todolist.app.data.preferences.SettingsRepository
import com.todolist.app.data.repository.TaskRepository
import com.todolist.app.domain.model.Category
import com.todolist.app.domain.model.Subtask
import com.todolist.app.domain.model.Task
import com.todolist.app.domain.model.Urgency
import com.todolist.app.ui.BaseViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class TaskFormState(
    val isEditing: Boolean = false,
    val title: String = "",
    val description: String = "",
    val dueDate: LocalDate = LocalDate.now(),
    val dueTime: LocalTime? = null,
    /** Nulo = selo automático calculado pelo prazo. */
    val manualUrgency: Urgency? = null,
    val categoryId: Long? = null,
    val subtasks: List<Subtask> = emptyList(),
    val titleError: String? = null,
    val isSaving: Boolean = false,
) {
    val autoUrgency: Urgency get() = Urgency.fromDueDate(dueDate, LocalDate.now())
    val effectiveUrgency: Urgency get() = manualUrgency ?: autoUrgency
    val canSave: Boolean get() = title.isNotBlank() && !isSaving
}

const val MAX_TITLE_LENGTH = 80

class TaskFormViewModel(
    private val taskId: Long?,
    private val repository: TaskRepository,
    private val settings: SettingsRepository,
) : BaseViewModel() {

    private val _saved = Channel<Unit>(Channel.CONFLATED)
    val saved = _saved.receiveAsFlow()

    /** Tarefa original (na edição), para preservar status e data de conclusão. */
    private var original: Task? = null

    private val _state = MutableStateFlow(TaskFormState(isEditing = taskId != null))
    val state: StateFlow<TaskFormState> = _state.asStateFlow()

    val categories: StateFlow<List<Category>> =
        repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var nextSubtaskId = -1L

    init {
        if (taskId == null) {
            viewModelScope.launch {
                // Com "Urgência automática" desligada, novas tarefas começam com selo manual.
                if (!settings.settings.first().autoUrgency) _state.update { it.copy(manualUrgency = Urgency.CALM) }
            }
        } else {
            viewModelScope.launch {
                val task = repository.observeTask(taskId).first()
                if (task == null) {
                    showMessage("Tarefa não encontrada.")
                    return@launch
                }
                original = task
                _state.value = TaskFormState(
                    isEditing = true,
                    title = task.title,
                    description = task.description,
                    dueDate = task.dueDate,
                    dueTime = task.dueTime,
                    manualUrgency = task.manualUrgency,
                    categoryId = task.categoryId,
                    subtasks = task.subtasks,
                )
            }
        }
    }

    fun onTitleChange(value: String) = _state.update { it.copy(title = value.take(MAX_TITLE_LENGTH), titleError = null) }
    fun onDescriptionChange(value: String) = _state.update { it.copy(description = value) }
    fun onDueDateChange(value: LocalDate) = _state.update { it.copy(dueDate = value) }
    fun onDueTimeChange(value: LocalTime?) = _state.update { it.copy(dueTime = value) }
    fun onCategoryChange(id: Long?) = _state.update { it.copy(categoryId = id) }

    /** Tocar no selo já escolhido volta para o modo automático. */
    fun onUrgencyChange(value: Urgency) = _state.update {
        it.copy(manualUrgency = if (it.manualUrgency == value) null else value)
    }

    fun addSubtask(title: String) {
        if (title.isBlank()) return
        _state.update { it.copy(subtasks = it.subtasks + Subtask(nextSubtaskId--, title.trim())) }
    }

    fun removeSubtask(id: Long) = _state.update { s -> s.copy(subtasks = s.subtasks.filterNot { it.id == id }) }

    fun save() {
        val s = _state.value
        if (s.isSaving) return
        if (s.title.isBlank()) {
            _state.update { it.copy(titleError = "Informe o título da tarefa.") }
            return
        }
        _state.update { it.copy(isSaving = true) }
        val base = original
        val task = Task(
            id = base?.id ?: 0,
            title = s.title,
            description = s.description,
            dueDate = s.dueDate,
            dueTime = s.dueTime,
            categoryId = s.categoryId,
            manualUrgency = s.manualUrgency,
            isDone = base?.isDone ?: false,
            completedAt = base?.completedAt,
            subtasks = s.subtasks,
        )
        launchSafely(
            errorMessage = "Não foi possível salvar a tarefa.",
            successMessage = if (base == null) "Tarefa criada" else "Tarefa atualizada",
            onSuccess = { _saved.trySend(Unit) },
        ) {
            try {
                repository.saveTask(task)
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }
}
