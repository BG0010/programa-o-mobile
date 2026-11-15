package com.todolist.app.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.todolist.app.domain.model.Task
import com.todolist.app.ui.CollectMessages
import com.todolist.app.ui.appContainer
import com.todolist.app.ui.components.ConfirmDialog
import com.todolist.app.ui.components.SwipeableTaskCard
import com.todolist.app.ui.components.EmptyState
import com.todolist.app.ui.theme.Blue500
import com.todolist.app.ui.theme.Blue600
import com.todolist.app.ui.theme.UrgentRed
import com.todolist.app.ui.theme.UrgentRedBg
import com.todolist.app.util.longLabel

@Composable
fun TasksScreen(
    onOpenTask: (Long) -> Unit,
    userName: String?,
) {
    val container = appContainer()
    val viewModel: TasksViewModel = viewModel { TasksViewModel(container.taskRepository) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<Task?>(null) }
    CollectMessages(viewModel)

    pendingDelete?.let { task ->
        ConfirmDialog(
            title = "Excluir tarefa?",
            message = "\"${task.title}\" será excluída permanentemente.",
            confirmText = "Excluir",
            onConfirm = {
                viewModel.delete(task)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { TasksHeader(state, userName) }
        item {
            SearchField(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        item {
            FilterRow(
                selected = state.filter,
                urgentCount = state.urgentCount,
                onSelect = viewModel::onFilterChange,
            )
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(state.filter.sectionTitle, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Ordenadas por prioridade",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val n = state.tasks.size
                Text(
                    if (n == 1) "1 item" else "$n itens",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (!state.isLoading && state.tasks.isEmpty()) {
            item {
                EmptyState(
                    when {
                        state.totalCount == 0 -> "Você ainda não tem tarefas.\nToque em + para criar a primeira."
                        state.query.isNotBlank() -> "Nenhuma tarefa encontrada para \"${state.query}\"."
                        else -> "Nenhuma tarefa neste filtro."
                    },
                )
            }
        }
        items(state.tasks, key = { it.id }) { task ->
            SwipeableTaskCard(
                task = task,
                category = task.categoryId?.let(state.categories::get),
                today = state.today,
                onClick = { onOpenTask(task.id) },
                onToggleDone = { viewModel.setDone(task, it) },
                modifier = Modifier.padding(horizontal = 16.dp),
                onSwipeDelete = { pendingDelete = task },
            )
        }
    }
}

@Composable
private fun TasksHeader(state: TasksUiState, userName: String?) {
    val shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(Blue500, Blue600)))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Check, null, tint = Blue500, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text("TodoList", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                // Etapa 5: passará a abrir os lembretes.
                Icon(Icons.Outlined.NotificationsNone, contentDescription = "Notificações", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            state.today.longLabel().uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            userName?.trim()?.substringBefore(' ')?.takeIf { it.isNotEmpty() }?.let { "Olá, $it!" } ?: "Olá!",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
        )
        Text(
            when (state.todayPending) {
                0 -> "Nenhuma tarefa pendente para hoje"
                1 -> "Você tem 1 tarefa para hoje"
                else -> "Você tem ${state.todayPending} tarefas para hoje"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                .padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Seu progresso hoje", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                    Text(
                        "${state.todayDone} de ${state.todayTotal} concluídas",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                    )
                }
                Text(
                    "${(state.todayProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { state.todayProgress },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
                drawStopIndicator = {},
                gapSize = 0.dp,
            )
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Buscar uma tarefa...") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}

@Composable
private fun FilterRow(selected: TaskFilter, urgentCount: Int, onSelect: (TaskFilter) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TaskFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(filter.label)
                        if (filter == TaskFilter.URGENT && urgentCount > 0) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "$urgentCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = UrgentRed,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(UrgentRedBg)
                                    .padding(horizontal = 6.dp),
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}
