package com.todolist.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.todolist.app.data.repository.HolidayRepository
import com.todolist.app.data.repository.TaskRepository
import com.todolist.app.domain.model.Category
import com.todolist.app.domain.model.Holiday
import com.todolist.app.domain.model.Task
import com.todolist.app.ui.appContainer
import com.todolist.app.ui.components.AppCard
import com.todolist.app.ui.components.EmptyState
import com.todolist.app.ui.components.IconTile
import com.todolist.app.ui.components.ScreenHeader
import com.todolist.app.ui.components.colors
import com.todolist.app.util.label
import com.todolist.app.util.longLabel
import com.todolist.app.util.monthYearLabel
import com.todolist.app.util.weekDayShortLabel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class CalendarUiState(
    val today: LocalDate = LocalDate.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val week: List<LocalDate> = emptyList(),
    val daysWithTasks: Set<LocalDate> = emptySet(),
    val tasksOfDay: List<Task> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val holidayDates: Set<LocalDate> = emptySet(),
    val holidaysOfDay: List<Holiday> = emptyList(),
    /** Mensagem quando os feriados não puderam ser baixados (o restante do calendário funciona normalmente). */
    val holidayError: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    repository: TaskRepository,
    private val holidayRepository: HolidayRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val holidayError = MutableStateFlow<String?>(null)

    /** Anos visíveis na faixa semanal (dois na virada de ano). */
    private val visibleYears = selectedDate
        .map { d -> setOf(d.minusDays(3).year, d.plusDays(3).year) }
        .distinctUntilChanged()

    private val holidays = visibleYears
        .onEach { years -> years.forEach { refreshHolidays(it) } }
        .flatMapLatest { years ->
            combine(years.map(holidayRepository::observe)) { lists -> lists.flatMap { it } }
        }

    val uiState: StateFlow<CalendarUiState> = combine(
        repository.tasks, repository.categories, selectedDate, holidays, holidayError,
    ) { tasks, categories, selected, holidays, error ->
        CalendarUiState(
            today = LocalDate.now(),
            selectedDate = selected,
            // Faixa de 7 dias com o dia selecionado no centro.
            week = (-3L..3L).map { selected.plusDays(it) },
            daysWithTasks = tasks.mapTo(HashSet()) { it.dueDate },
            tasksOfDay = tasks.filter { it.dueDate == selected }
                .sortedWith(compareBy<Task>({ it.isDone }, { it.dueTime })),
            categories = categories.associateBy { it.id },
            holidayDates = holidays.mapTo(HashSet()) { it.date },
            holidaysOfDay = holidays.filter { it.date == selected },
            holidayError = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    private fun refreshHolidays(year: Int) {
        viewModelScope.launch {
            holidayRepository.refresh(year)
                .onSuccess { holidayError.value = null }
                .onFailure { holidayError.value = it.message }
        }
    }

    fun retryHolidays() {
        val d = selectedDate.value
        setOf(d.minusDays(3).year, d.plusDays(3).year).forEach { refreshHolidays(it) }
    }

    fun select(date: LocalDate) { selectedDate.value = date }
    fun previousWeek() { selectedDate.value = selectedDate.value.minusWeeks(1) }
    fun nextWeek() { selectedDate.value = selectedDate.value.plusWeeks(1) }
}

@Composable
fun CalendarScreen(onOpenTask: (Long) -> Unit) {
    val container = appContainer()
    val viewModel: CalendarViewModel = viewModel {
        CalendarViewModel(container.taskRepository, container.holidayRepository)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Calendário", "Veja seus prazos em um só lugar")
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = viewModel::previousWeek) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Semana anterior")
                    }
                    Text(
                        state.selectedDate.monthYearLabel(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    FilledTonalIconButton(onClick = viewModel::nextWeek) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próxima semana")
                    }
                }
            }
            item { WeekStrip(state, viewModel::select) }
            item { SelectedDayCard(state) }
            items(state.holidaysOfDay, key = { "holiday-" + it.date }) { holiday -> HolidayCard(holiday) }
            state.holidayError?.let { message ->
                item { HolidayErrorCard(message, onRetry = viewModel::retryHolidays) }
            }
            item {
                Column {
                    Text("Tarefas do dia", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Mantenha o foco no que importa",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (state.tasksOfDay.isEmpty()) {
                item { EmptyState("Nenhuma tarefa para este dia.") }
            }
            items(state.tasksOfDay, key = { it.id }) { task ->
                DayTaskRow(task, task.categoryId?.let(state.categories::get), state.today) { onOpenTask(task.id) }
            }
        }
    }
}

@Composable
private fun WeekStrip(state: CalendarUiState, onSelect: (LocalDate) -> Unit) {
    AppCard(Modifier.fillMaxWidth(), contentPadding = 8) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            state.week.forEach { date ->
                val selected = date == state.selectedDate
                val hasTasks = date in state.daysWithTasks
                val isHoliday = date in state.holidayDates
                val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onSelect(date) }
                        .semantics {
                            contentDescription = date.longLabel() +
                                (if (isHoliday) ", feriado" else "") + (if (hasTasks) ", com tarefas" else "")
                        }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(date.weekDayShortLabel(), style = MaterialTheme.typography.labelSmall, color = contentColor)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${date.dayOfMonth}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    // Ponto azul = tarefas; ponto laranja = feriado.
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Box(
                            Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        !hasTasks -> Color.Transparent
                                        selected -> Color.White
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                ),
                        )
                        if (isHoliday) Box(Modifier.size(4.dp).clip(CircleShape).background(HolidayColor))
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDayCard(state: CalendarUiState) {
    AppCard(
        Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        borderColor = MaterialTheme.colorScheme.primaryContainer,
        contentPadding = 12,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(
                Icons.Outlined.CalendarToday,
                tint = MaterialTheme.colorScheme.primary,
                container = MaterialTheme.colorScheme.surface,
                size = 40,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Data selecionada", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(state.selectedDate.longLabel(), style = MaterialTheme.typography.titleSmall)
            }
            Text(
                "${state.tasksOfDay.size}",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun DayTaskRow(task: Task, category: Category?, today: LocalDate, onClick: () -> Unit) {
    AppCard(Modifier.fillMaxWidth(), onClick = onClick, contentPadding = 14) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(task.urgency(today).colors().content))
            Spacer(Modifier.width(10.dp))
            Text(
                task.dueTime?.label() ?: "--:--",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(44.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                    color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    (category?.name ?: "Sem categoria") + if (task.isDone) " · Concluída" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val HolidayColor = Color(0xFFF59E0B)

@Composable
private fun HolidayCard(holiday: Holiday) {
    AppCard(
        Modifier.fillMaxWidth(),
        containerColor = HolidayColor.copy(alpha = 0.12f),
        borderColor = HolidayColor.copy(alpha = 0.4f),
        contentPadding = 12,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(Icons.Outlined.Celebration, tint = HolidayColor, size = 40)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Feriado nacional", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(holiday.name, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun HolidayErrorCard(message: String, onRetry: () -> Unit) {
    AppCard(Modifier.fillMaxWidth(), contentPadding = 12) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(Icons.Outlined.CloudOff, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Feriados indisponíveis", style = MaterialTheme.typography.titleSmall)
                Text(message, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onRetry) { Text("Tentar de novo") }
        }
    }
}
