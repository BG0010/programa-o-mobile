package com.todolist.app.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.todolist.app.domain.model.Urgency
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)],
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Sempre salvo em minúsculas e sem espaços nas pontas. */
    val email: String,
    /** Hash PBKDF2 da senha (Base64). A senha em texto puro nunca é salva. */
    val passwordHash: String,
    val salt: String,
    val createdAt: Long,
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(UserEntity::class, ["id"], ["userId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("userId")],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val colorArgb: Long,
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(UserEntity::class, ["id"], ["userId"], onDelete = ForeignKey.CASCADE),
        // Excluir uma categoria não exclui as tarefas: elas ficam "Sem categoria".
        ForeignKey(CategoryEntity::class, ["id"], ["categoryId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("userId"), Index("categoryId")],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val description: String,
    val dueDate: LocalDate,
    val dueTime: LocalTime?,
    val categoryId: Long?,
    val manualUrgency: Urgency?,
    val isDone: Boolean,
    val completedAt: LocalDate?,
)

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(TaskEntity::class, ["id"], ["taskId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("taskId")],
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val title: String,
    val isDone: Boolean,
    /** Ordem de exibição na lista. */
    val position: Int,
)

/** Tarefa com suas subtarefas, carregada em uma única consulta pelo Room. */
data class TaskWithSubtasks(
    @Embedded val task: TaskEntity,
    @Relation(parentColumn = "id", entityColumn = "taskId")
    val subtasks: List<SubtaskEntity>,
)
