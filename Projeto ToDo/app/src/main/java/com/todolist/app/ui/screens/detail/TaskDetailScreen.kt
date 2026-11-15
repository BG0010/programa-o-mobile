package com.todolist.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.todolist.app.data.repository.TaskRepository
import com.todolist.app.domain.model.Category
import com.todolist.app.domain.model.Task
import com.todolist.app.ui.BaseViewModel
import com.todolist.app.ui.CollectMessages
import com.todolist.app.ui.appContainer
import com.todolist.app.ui.components.ConfirmDialog
import com.todolist.app.ui.components.AppCard
import com.todolist.app.ui.components.CategoryChip
import com.todolist.app.ui.components.EmptyState
import com.todolist.app.ui.components.InfoRow
import com.todolist.app.ui.components.Pill
import com.todolist.app.ui.components.UrgencyBadge
import com.todolist.app.ui.components.colors
import com.todolist.app.util.label
import com.todolist.app.util.longLabel
import com.todolist.app.util.relativeDueLabel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class TaskDetailUiState(
    val isLoading: Boolean = true,
    /** Verdadeiro durante e após a exclusão, para não exibir "tarefa não existe" antes de voltar. */
    val isDeleting: Boolean = false,
    val task: Task? = null,
    val category: Category? = null,
)

class TaskDetailViewModel(
    private val taskId: Long,
    private val repository: TaskRepository,
) : BaseViewModel() {
    private val deleting = MutableStateFlow(false)
    private val _deleted = Channel<Unit>(Channel.CONFLATED)
    val deleted = _deleted.receiveAsFlow()

    val uiState: StateFlow<TaskDetailUiState> = combine(
        repository.observeTask(taskId), repository.categories, deleting,
    ) { task, categories, deleting ->
        TaskDetailUiState(
            isLoading = false,
            isDeleting = deleting,
            task = task,
            category = categories.find { it.id == task?.categoryId },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskDetailUiState())

    fun setDone(done: Boolean) = launchSafely(
        errorMessage = "Não foi possível atualizar a tarefa.",
        successMessage = if (done) "Tarefa concluída" else "Tarefa marcada como pendente",
    ) { repository.setTaskDone(taskId, done) }

    fun setSubtaskDone(subtaskId: Long, done: Boolean) = launchSafely(
        errorMessage = "Não foi possível atualizar a subtarefa.",
    ) { repository.setSubtaskDone(taskId, subtaskId, done) }

    fun delete() {
        deleting.value = true
        launchSafely(
            errorMessage = "Não foi possível excluir a tarefa.",
            successMessage = "Tarefa excluída",
            onSuccess = { _deleted.trySend(Unit) },
        ) {
            try {
                repository.deleteTask(taskId)
            } catch (e: Exception) {
                deleting.value = false
                throw e
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    val container = appContainer()
    val viewModel: TaskDetailViewModel = viewModel(key = "detail-$taskId") {
        TaskDetailViewModel(taskId, container.taskRepository)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    var confirmDelete by remember { mutableStateOf(false) }
    CollectMessages(viewModel)
    LaunchedEffect(viewModel) { viewModel.deleted.collect { onBack() } }

    if (confirmDelete) {
        ConfirmDialog(
            title = "Excluir tarefa?",
            message = "Esta tarefa e suas subtarefas serão excluídas permanentemente.",
            confirmText = "Excluir",
            onConfirm = {
                confirmDelete = false
                viewModel.delete()
            },
            onDismiss = { confirmDelete = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da tarefa") },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (state.task != null && !state.isDeleting) {
                        FilledTonalIconButton(onClick = { onEdit(taskId) }, modifier = Modifier.padding(end = 8.dp)) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Editar tarefa")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            val task = state.task
            if (task != null && !state.isDeleting) {
                DetailBottomBar(
                    task = task,
                    onToggleDone = { viewModel.setDone(!task.isDone) },
                    onDelete = { confirmDelete = true },
                )
            }
        },
    ) { padding ->
        val task = state.task
        when {
            state.isLoading || state.isDeleting -> Unit
            task == null -> EmptyState("Esta tarefa não existe mais.", Modifier.padding(padding))
            else -> DetailContent(task, state.category, today, viewModel::setSubtaskDone, Modifier.padding(padding))
        }
    }
}

@Composable
private fun DetailContent(
    task: Task,
    category: Category?,
    today: LocalDate,
    onSubtaskToggle: (Long, Boolean) -> Unit,
    modifier: Modifier,
) {
    val urgency = task.urgency(today)
    val urgencyColors = urgency.colors()
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(urgencyColors.container),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Flag, contentDescription = null, tint = urgencyColors.content)
        }
        Spacer(Modifier.height(10.dp))
        UrgencyBadge(urgency)
        Spacer(Modifier.height(10.dp))
        Text(
            task.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
        )
        if (task.description.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                task.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(20.dp))
        AppCard(Modifier.fillMaxWidth(), contentPadding = 12) {
            InfoRow(Icons.Outlined.CalendarToday, "Data de entrega", task.dueDate.longLabel()) {
                if (!task.isDone) {
                    Pill(relativeDueLabel(task.dueDate, today), urgencyColors.content, urgencyColors.container)
                }
            }
            task.dueTime?.let {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                InfoRow(Icons.Outlined.Schedule, "Horário", it.label())
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            InfoRow(Icons.Outlined.Checklist, "Categoria", category?.name ?: "Sem categoria") {
                category?.let { CategoryChip(it) }
            }
        }
        if (task.subtasks.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            AppCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Checklist", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(
                        "${task.completedSubtasks} de ${task.subtasks.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { task.completedSubtasks.toFloat() / task.subtasks.size },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                    drawStopIndicator = {},
                gapSize = 0.dp,
                )
                Spacer(Modifier.height(8.dp))
                task.subtasks.forEachIndexed { index, subtask ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    // A linha inteira é tocável (área maior e um único item para o leitor de tela).
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = subtask.isDone,
                                role = Role.Checkbox,
                                onValueChange = { onSubtaskToggle(subtask.id, it) },
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = subtask.isDone, onCheckedChange = null)
                        Text(
                            subtask.title,
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = if (subtask.isDone) TextDecoration.LineThrough else null,
                            color = if (subtask.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBottomBar(task: Task, onToggleDone: () -> Unit, onDelete: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(
                onClick = onDelete,
                modifier = Modifier.size(52.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = "Excluir tarefa")
            }
            val colors = if (task.isDone) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                ButtonDefaults.buttonColors()
            }
            Button(
                onClick = onToggleDone,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = colors,
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (task.isDone) "Marcar como pendente" else "Concluir tarefa")
            }
        }
    }
}
