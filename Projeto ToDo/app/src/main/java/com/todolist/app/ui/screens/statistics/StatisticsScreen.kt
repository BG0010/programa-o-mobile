package com.todolist.app.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.todolist.app.data.repository.TaskRepository
import com.todolist.app.domain.model.Category
import com.todolist.app.domain.model.Urgency
import com.todolist.app.ui.appContainer
import com.todolist.app.ui.components.AppCard
import com.todolist.app.ui.components.IconTile
import com.todolist.app.ui.components.ScreenHeader
import com.todolist.app.ui.components.color
import com.todolist.app.ui.theme.Blue500
import com.todolist.app.ui.theme.Blue600
import com.todolist.app.ui.theme.UrgentRed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class DayCount(val date: LocalDate, val count: Int)
data class CategoryCount(val category: Category, val count: Int)

data class StatisticsUiState(
    val today: LocalDate = LocalDate.now(),
    /** Tarefas com prazo na semana atual (seg–dom). */
    val weekTotal: Int = 0,
    val weekDone: Int = 0,
    val completedThisWeek: Int = 0,
    val overdue: Int = 0,
    val last7Days: List<DayCount> = emptyList(),
    val byCategory: List<CategoryCount> = emptyList(),
) {
    val weekProductivity: Float get() = if (weekTotal == 0) 0f else weekDone.toFloat() / weekTotal
}

class StatisticsViewModel(repository: TaskRepository) : ViewModel() {
    val uiState: StateFlow<StatisticsUiState> = combine(repository.tasks, repository.categories) { tasks, categories ->
        val today = LocalDate.now()
        val weekStart = today.with(DayOfWeek.MONDAY)
        val weekEnd = weekStart.plusDays(6)
        val inWeek = { d: LocalDate? -> d != null && !d.isBefore(weekStart) && !d.isAfter(weekEnd) }
        val weekTasks = tasks.filter { inWeek(it.dueDate) }

        StatisticsUiState(
            today = today,
            weekTotal = weekTasks.size,
            weekDone = weekTasks.count { it.isDone },
            completedThisWeek = tasks.count { it.isDone && inWeek(it.completedAt) },
            overdue = tasks.count { !it.isDone && it.urgency(today) == Urgency.OVERDUE },
            last7Days = (6 downTo 0).map { offset ->
                val day = today.minusDays(offset.toLong())
                DayCount(day, tasks.count { it.isDone && it.completedAt == day })
            },
            byCategory = categories.map { c -> CategoryCount(c, tasks.count { it.categoryId == c.id }) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())
}

@Composable
fun StatisticsScreen() {
    val container = appContainer()
    val viewModel: StatisticsViewModel = viewModel { StatisticsViewModel(container.taskRepository) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Estatísticas", "Um resumo do seu ritmo")
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProductivityCard(state)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CounterCard(
                    Modifier.weight(1f),
                    icon = Icons.Filled.Check, tint = MaterialTheme.colorScheme.primary,
                    value = state.completedThisWeek, label = "Concluídas", caption = "nesta semana",
                )
                CounterCard(
                    Modifier.weight(1f),
                    icon = Icons.Outlined.Schedule, tint = UrgentRed,
                    value = state.overdue, label = "Atrasadas", caption = "precisam de atenção",
                )
            }
            CompletedChart(state)
            CategoryCard(state)
        }
    }
}

@Composable
private fun ProductivityCard(state: StatisticsUiState) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Blue500, Blue600)))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Produtividade semanal", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
            Text(
                "${(state.weekProductivity * 100).toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
            )
            Text(
                if (state.weekTotal == 0) "Nenhuma tarefa com prazo nesta semana"
                else "${state.weekDone} de ${state.weekTotal} tarefas da semana concluídas",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
        CircularProgressIndicator(
            progress = { state.weekProductivity },
            modifier = Modifier.size(56.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
            strokeWidth = 6.dp,
            gapSize = 0.dp,
        )
    }
}

@Composable
private fun CounterCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    value: Int,
    label: String,
    caption: String,
) {
    AppCard(modifier) {
        IconTile(icon, tint = tint)
        Spacer(Modifier.height(12.dp))
        Text("$value", style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(caption, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CompletedChart(state: StatisticsUiState) {
    val max = (state.last7Days.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    val ptBr = Locale.forLanguageTag("pt-BR")
    AppCard(Modifier.fillMaxWidth()) {
        Text("Tarefas concluídas", style = MaterialTheme.typography.titleMedium)
        Text("Últimos 7 dias", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.last7Days.forEach { day ->
                val isToday = day.date == state.today
                val dayName = day.date.dayOfWeek.getDisplayName(TextStyle.FULL, ptBr)
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clearAndSetSemantics { contentDescription = "$dayName: ${day.count} concluídas" },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("${day.count}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(
                        Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        if (day.count > 0) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(day.count.toFloat() / max)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isToday) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                    ),
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        dayName.take(1).uppercase(ptBr),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(state: StatisticsUiState) {
    val max = (state.byCategory.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    AppCard(Modifier.fillMaxWidth()) {
        Text("Por categoria", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        state.byCategory.forEach { item ->
            Column(Modifier.padding(vertical = 6.dp).semantics(mergeDescendants = true) {}) {
                Row(Modifier.fillMaxWidth()) {
                    Text(item.category.name, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                    Text(
                        if (item.count == 1) "1 tarefa" else "${item.count} tarefas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { item.count.toFloat() / max },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                    color = item.category.color(),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    drawStopIndicator = {},
                gapSize = 0.dp,
                )
            }
        }
    }
}
