package com.todolist.app.data.repository

import com.todolist.app.domain.model.Category
import com.todolist.app.domain.model.Task
import kotlinx.coroutines.flow.Flow

/** Tarefas e categorias do usuário logado. */
interface TaskRepository {
    val tasks: Flow<List<Task>>
    val categories: Flow<List<Category>>
    fun observeTask(id: Long): Flow<Task?>

    /** Cria (id = 0) ou atualiza a tarefa. Retorna o id salvo. */
    suspend fun saveTask(task: Task): Long
    suspend fun deleteTask(id: Long)
    suspend fun setTaskDone(id: Long, done: Boolean)
    suspend fun setSubtaskDone(taskId: Long, subtaskId: Long, done: Boolean)

    /** Cria (id = 0) ou atualiza a categoria. */
    suspend fun saveCategory(category: Category): Long
    suspend fun deleteCategory(id: Long)
    suspend fun isCategoryNameTaken(name: String, excludeId: Long = 0): Boolean
}
