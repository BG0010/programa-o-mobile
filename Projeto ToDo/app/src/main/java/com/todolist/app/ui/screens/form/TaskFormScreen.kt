package com.todolist.app.ui.screens.form

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.todolist.app.domain.model.Urgency
import com.todolist.app.ui.CollectMessages
import com.todolist.app.ui.appContainer
import com.todolist.app.ui.components.AppCard
import com.todolist.app.ui.components.color
import com.todolist.app.ui.components.colors
import com.todolist.app.util.label
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val FieldDate = DateTimeFormatter.ofPattern("dd / MM / yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    taskId: Long?,
    onBack: () -> Unit,
) {
    val container = appContainer()
    val viewModel: TaskFormViewModel = viewModel(key = "form-${taskId ?: "new"}") {
        TaskFormViewModel(taskId, container.taskRepository, container.settingsRepository)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    CollectMessages(viewModel)
    LaunchedEffect(viewModel) { viewModel.saved.collect { onBack() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (state.isEditing) "Editar tarefa" else "Nova tarefa")
                        Text(
                            "Organize seu prazo com calma",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Button(
                    onClick = viewModel::save,
                    enabled = state.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Salvar tarefa")
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FieldLabel("Título da tarefa")
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                placeholder = { Text("O que precisa ser feito?") },
                isError = state.titleError != null,
                supportingText = {
                    Text(state.titleError ?: "${state.title.length}/$MAX_TITLE_LENGTH")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            FieldLabel("Descrição", optional = true)
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                placeholder = { Text("Adicione detalhes importantes...") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp),
            )
            DueDateSection(state, viewModel::onDueDateChange, viewModel::onDueTimeChange)
            UrgencySection(state, viewModel::onUrgencyChange)

            AppCard(Modifier.fillMaxWidth()) {
                Text("Categoria", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(10.dp))
                if (categories.isEmpty()) {
                    Text(
                        "Nenhuma categoria. Crie categorias em Ajustes › Categorias.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categories.forEach { category ->
                        val selected = state.categoryId == category.id
                        OptionButton(
                            text = category.name,
                            selected = selected,
                            selectedColor = category.color(),
                            onClick = { viewModel.onCategoryChange(if (selected) null else category.id) },
                        )
                    }
                }
            }

            SubtasksSection(
                subtasks = state.subtasks.map { it.id to it.title },
                onAdd = viewModel::addSubtask,
                onRemove = viewModel::removeSubtask,
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String, optional: Boolean = false) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(text, style = MaterialTheme.typography.titleSmall)
        if (optional) {
            Spacer(Modifier.width(4.dp))
            Text("Opcional", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
)

/** Botão de opção selecionável usado em data rápida, selo e categoria. */
@Composable
private fun OptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    leading: (@Composable () -> Unit)? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) selectedColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (selected) selectedColor else Color.Transparent),
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            leading?.invoke()
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) selectedColor else contentColor,
                maxLines = 1,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDateSection(
    state: TaskFormState,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime?) -> Unit,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val today = LocalDate.now()

    AppCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Data de entrega", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            if (state.manualUrgency == null) {
                Text("Selo automático", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Hoje" to 0L, "Amanhã" to 1L, "Em 3 dias" to 3L).forEach { (label, offset) ->
                OptionButton(
                    text = label,
                    selected = state.dueDate == today.plusDays(offset),
                    onClick = { onDateChange(today.plusDays(offset)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PickerField(
                icon = Icons.Outlined.CalendarToday,
                text = state.dueDate.format(FieldDate),
                contentDescription = "Escolher data",
                onClick = { showDatePicker = true },
                modifier = Modifier.weight(1.3f),
            )
            PickerField(
                icon = Icons.Outlined.Schedule,
                text = state.dueTime?.label() ?: "-- : --",
                contentDescription = "Escolher horário",
                onClick = { showTimePicker = true },
                onClear = if (state.dueTime != null) ({ onTimeChange(null) }) else null,
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dueDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        onDateChange(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = pickerState) }
    }

    if (showTimePicker) {
        val initial = state.dueTime ?: LocalTime.now().withMinute(0)
        val timeState = rememberTimePickerState(initial.hour, initial.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange(LocalTime.of(timeState.hour, timeState.minute))
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } },
            text = { TimePicker(state = timeState) },
        )
    }
}

@Composable
private fun PickerField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (onClear != null) {
                IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Remover horário", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun UrgencySection(state: TaskFormState, onSelect: (Urgency) -> Unit) {
    AppCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Selo de urgência", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(
                if (state.manualUrgency == null) "Automático" else "Manual",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        val options = listOf(Urgency.URGENT, Urgency.ATTENTION, Urgency.CALM, Urgency.OVERDUE)
        options.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                row.forEach { urgency ->
                    val c = urgency.colors()
                    OptionButton(
                        text = urgency.label,
                        selected = state.effectiveUrgency == urgency,
                        selectedColor = c.content,
                        contentColor = c.content,
                        onClick = { onSelect(urgency) },
                        leading = {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(c.content))
                            Spacer(Modifier.width(6.dp))
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Text(
            if (state.manualUrgency == null) "Calculado pela data de entrega. Toque em um selo para escolher manualmente."
            else "Toque novamente no selo escolhido para voltar ao automático.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SubtasksSection(
    subtasks: List<Pair<Long, String>>,
    onAdd: (String) -> Unit,
    onRemove: (Long) -> Unit,
) {
    var newSubtask by remember { mutableStateOf("") }
    AppCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Subtarefas", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(
                if (subtasks.size == 1) "1 item" else "${subtasks.size} itens",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        subtasks.forEach { (id, title) ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { onRemove(id) }) {
                    Icon(Icons.Filled.Close, contentDescription = "Remover subtarefa $title", modifier = Modifier.size(18.dp))
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        val submit = {
            onAdd(newSubtask)
            newSubtask = ""
        }
        OutlinedTextField(
            value = newSubtask,
            onValueChange = { newSubtask = it },
            placeholder = { Text("Adicionar subtarefa") },
            leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (newSubtask.isNotBlank()) {
                    IconButton(onClick = submit) { Icon(Icons.Filled.Check, contentDescription = "Adicionar") }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
