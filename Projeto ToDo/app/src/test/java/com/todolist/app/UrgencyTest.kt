package com.todolist.app

import com.todolist.app.domain.model.Task
import com.todolist.app.domain.model.Urgency
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class UrgencyTest {
    private val today = LocalDate.of(2026, 11, 15)

    @Test
    fun `selo automatico segue o prazo`() {
        assertEquals(Urgency.OVERDUE, Urgency.fromDueDate(today.minusDays(1), today))
        assertEquals(Urgency.URGENT, Urgency.fromDueDate(today, today))
        assertEquals(Urgency.URGENT, Urgency.fromDueDate(today.plusDays(1), today))
        assertEquals(Urgency.ATTENTION, Urgency.fromDueDate(today.plusDays(2), today))
        assertEquals(Urgency.ATTENTION, Urgency.fromDueDate(today.plusDays(3), today))
        assertEquals(Urgency.CALM, Urgency.fromDueDate(today.plusDays(4), today))
    }

    @Test
    fun `selo manual prevalece sobre o automatico`() {
        val task = Task(id = 1, title = "x", dueDate = today.plusDays(10), manualUrgency = Urgency.URGENT)
        assertEquals(Urgency.URGENT, task.urgency(today))
    }

    @Test
    fun `tarefa pendente vencida e sempre atrasada mesmo com selo manual`() {
        val task = Task(id = 1, title = "x", dueDate = today.minusDays(2), manualUrgency = Urgency.CALM)
        assertEquals(Urgency.OVERDUE, task.urgency(today))
    }

    @Test
    fun `tarefa concluida vencida nao aparece como atrasada se tiver selo manual`() {
        val task = Task(id = 1, title = "x", dueDate = today.minusDays(2), manualUrgency = Urgency.CALM, isDone = true)
        assertEquals(Urgency.CALM, task.urgency(today))
    }
}
