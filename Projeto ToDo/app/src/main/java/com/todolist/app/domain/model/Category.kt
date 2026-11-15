package com.todolist.app.domain.model

data class Category(
    val id: Long,
    val name: String,
    /** Cor no formato ARGB (ex.: 0xFF3B6FE0). */
    val colorArgb: Long,
)
