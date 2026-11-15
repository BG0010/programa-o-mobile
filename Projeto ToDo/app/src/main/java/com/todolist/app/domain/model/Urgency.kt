package com.todolist.app.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Selo de urgência de uma tarefa. A ordem define a prioridade na listagem. */
enum class Urgency(val label: String) {
    OVERDUE("Atrasada"),
    URGENT("Urgente"),
    ATTENTION("Atenção"),
    CALM("Tranquilo");

    companion object {
        /**
         * Cálculo automático pelo prazo:
         * vencida → Atrasada; hoje ou amanhã → Urgente; até 3 dias → Atenção; depois → Tranquilo.
         */
        fun fromDueDate(dueDate: LocalDate, today: LocalDate): Urgency {
            val days = ChronoUnit.DAYS.between(today, dueDate)
            return when {
                days < 0 -> OVERDUE
                days <= 1 -> URGENT
                days <= 3 -> ATTENTION
                else -> CALM
            }
        }
    }
}
