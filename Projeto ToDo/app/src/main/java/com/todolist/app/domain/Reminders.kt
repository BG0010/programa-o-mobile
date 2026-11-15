package com.todolist.app.domain

import com.todolist.app.domain.model.Task
import java.time.LocalDateTime
import java.time.LocalTime

/** Regras de quando o lembrete de uma tarefa é disparado. */
object Reminders {
    /** Antecedência do lembrete para tarefas com horário. */
    const val MINUTES_BEFORE = 30L

    /** Horário do lembrete para tarefas sem horário definido. */
    val DEFAULT_TIME: LocalTime = LocalTime.of(9, 0)

    /** Com horário: 30 min antes. Sem horário: às 9h do dia do prazo. */
    fun reminderAt(task: Task): LocalDateTime =
        task.dueTime?.let { task.dueDate.atTime(it).minusMinutes(MINUTES_BEFORE) }
            ?: task.dueDate.atTime(DEFAULT_TIME)

    /** Tarefas pendentes cujo lembrete ainda vai acontecer, da mais próxima para a mais distante. */
    fun upcoming(tasks: List<Task>, now: LocalDateTime): List<Pair<Task, LocalDateTime>> =
        tasks.asSequence()
            .filter { !it.isDone }
            .map { it to reminderAt(it) }
            .filter { (_, at) -> at.isAfter(now) }
            .sortedBy { (_, at) -> at }
            .toList()
}
