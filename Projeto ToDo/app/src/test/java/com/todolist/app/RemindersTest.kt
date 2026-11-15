package com.todolist.app

import com.todolist.app.domain.Reminders
import com.todolist.app.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class RemindersTest {
    private val day = LocalDate.of(2026, 11, 16)

    @Test
    fun `com horario o lembrete e 30 minutos antes`() {
        val task = Task(1, "x", dueDate = day, dueTime = LocalTime.of(20, 0))
        assertEquals(day.atTime(19, 30), Reminders.reminderAt(task))
    }

    @Test
    fun `sem horario o lembrete e as 9h do dia`() {
        assertEquals(day.atTime(9, 0), Reminders.reminderAt(Task(1, "x", dueDate = day)))
    }

    @Test
    fun `proximos ignora concluidas e lembretes passados e ordena por horario`() {
        val now = LocalDateTime.of(2026, 11, 16, 12, 0)
        val tasks = listOf(
            Task(1, "passado", dueDate = day), // 9h de hoje, já passou
            Task(2, "concluida", dueDate = day.plusDays(1), isDone = true),
            Task(3, "amanha", dueDate = day.plusDays(1)),
            Task(4, "hoje a noite", dueDate = day, dueTime = LocalTime.of(20, 0)),
        )
        assertEquals(listOf(4L, 3L), Reminders.upcoming(tasks, now).map { it.first.id })
    }
}
