package com.todolist.app.domain.model

import java.time.LocalDate

/** Feriado nacional obtido da BrasilAPI. */
data class Holiday(
    val date: LocalDate,
    val name: String,
)
