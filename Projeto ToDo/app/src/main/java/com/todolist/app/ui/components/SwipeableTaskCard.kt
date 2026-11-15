package com.todolist.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.todolist.app.domain.model.Category
import com.todolist.app.domain.model.Task
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Card de tarefa que pode ser arrastado para a esquerda para excluir.
 * O card volta ao lugar: a exclusão só acontece após a confirmação no diálogo.
 */
@Composable
fun SwipeableTaskCard(
    task: Task,
    category: Category?,
    today: LocalDate,
    onClick: () -> Unit,
    onToggleDone: (Boolean) -> Unit,
    onSwipeDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        onDismiss = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onSwipeDelete()
            scope.launch { state.reset() }
        },
        modifier = modifier.semantics {
            // Alternativa acessível ao gesto de arrastar (TalkBack).
            customActions = listOf(CustomAccessibilityAction("Excluir tarefa") { onSwipeDelete(); true })
        },
        backgroundContent = {
            Row(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.85f))
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Excluir", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onError)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onError)
            }
        },
    ) {
        TaskCard(task = task, category = category, today = today, onClick = onClick, onToggleDone = onToggleDone)
    }
}
