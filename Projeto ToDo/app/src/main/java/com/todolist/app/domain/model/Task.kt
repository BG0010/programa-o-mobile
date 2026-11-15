package com.todolist.app.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Subtask(
    val id: Long,
    val title: String,
    val isDone: Boolean = false,
)

data class Task(
    val id: Long,
    val title: String,
    val description: String = "",
    val dueDate: LocalDate,
    val dueTime: LocalTime? = null,
    val categoryId: Long? = null,
    /** Selo escolhido manualmente; quando nulo, o selo é calculado pelo prazo. */
    val manualUrgency: Urgency? = null,
    val isDone: Boolean = false,
    /** Data em que a tarefa foi concluída (usada nas estatísticas). */
    val completedAt: LocalDate? = null,
    val subtasks: List<Subtask> = emptyList(),
) {
    fun urgency(today: LocalDate): Urgency {
        if (!isDone && dueDate.isBefore(today)) return Urgency.OVERDUE
        return manualUrgency ?: Urgency.fromDueDate(dueDate, today)
    }

    val completedSubtasks: Int get() = subtasks.count { it.isDone }
}
