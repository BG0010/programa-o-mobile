package com.todolist.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.todolist.app.domain.model.Category
import com.todolist.app.domain.model.Task
import com.todolist.app.domain.model.Urgency
import com.todolist.app.util.label
import com.todolist.app.util.relativeDueLabel
import com.todolist.app.util.shortLabel
import java.time.LocalDate

@Composable
fun TaskCard(
    task: Task,
    category: Category?,
    today: LocalDate,
    onClick: () -> Unit,
    onToggleDone: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val urgency = task.urgency(today)
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val isOverdue = urgency == Urgency.OVERDUE && !task.isDone
    AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        borderColor = if (isOverdue) urgency.colors().content.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = onToggleDone,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                        color = if (task.isDone) muted else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    UrgencyBadge(urgency)
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, null, tint = muted, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    val dateText = task.dueDate.shortLabel() + (task.dueTime?.let { ", ${it.label()}" } ?: "")
                    Text(dateText, style = MaterialTheme.typography.labelSmall, color = muted)
                    if (!task.isDone) {
                        Text("  |  ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(
                            relativeDueLabel(task.dueDate, today),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) urgency.colors().content else muted,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (category != null) CategoryChip(category) else Spacer(Modifier)
                    if (task.subtasks.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Checklist, null, tint = muted, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${task.completedSubtasks}/${task.subtasks.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = muted,
                            )
                        }
                    }
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Abrir detalhes",
                tint = muted,
                modifier = Modifier.padding(start = 4.dp).align(Alignment.CenterVertically),
            )
        }
    }
}
