package com.todolist.app.data.repository

import com.todolist.app.data.local.AppDatabase
import com.todolist.app.data.local.CategoryEntity
import com.todolist.app.data.local.SubtaskEntity
import com.todolist.app.data.local.TaskEntity
import com.todolist.app.data.local.TaskWithSubtasks
import com.todolist.app.data.preferences.SettingsRepository
import com.todolist.app.domain.model.Category
import com.todolist.app.domain.model.Subtask
import com.todolist.app.domain.model.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class RoomTaskRepository(
    db: AppDatabase,
    private val settings: SettingsRepository,
) : TaskRepository {

    private val taskDao = db.taskDao()
    private val categoryDao = db.categoryDao()

    /** Sempre que o usuário logado muda, as consultas passam a observar os dados dele. */
    override val tasks: Flow<List<Task>> = settings.sessionUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList()) else taskDao.observeAll(userId).map { list -> list.map { it.toDomain() } }
    }

    override val categories: Flow<List<Category>> = settings.sessionUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList()) else categoryDao.observeAll(userId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeTask(id: Long): Flow<Task?> = settings.sessionUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(null) else taskDao.observe(id, userId).map { it?.toDomain() }
    }

    override suspend fun saveTask(task: Task): Long {
        val userId = requireUserId()
        val entity = TaskEntity(
            id = task.id,
            userId = userId,
            title = task.title.trim(),
            description = task.description.trim(),
            dueDate = task.dueDate,
            dueTime = task.dueTime,
            categoryId = task.categoryId,
            manualUrgency = task.manualUrgency,
            isDone = task.isDone,
            completedAt = task.completedAt,
        )
        val subtasks = task.subtasks.map { SubtaskEntity(taskId = task.id, title = it.title, isDone = it.isDone, position = 0) }
        return taskDao.save(entity, subtasks)
    }

    override suspend fun deleteTask(id: Long) = taskDao.delete(id, requireUserId())

    override suspend fun setTaskDone(id: Long, done: Boolean) =
        taskDao.setDone(id, requireUserId(), done, if (done) LocalDate.now() else null)

    override suspend fun setSubtaskDone(taskId: Long, subtaskId: Long, done: Boolean) =
        taskDao.setSubtaskDone(taskId, subtaskId, requireUserId(), done)

    override suspend fun saveCategory(category: Category): Long {
        val entity = CategoryEntity(category.id, requireUserId(), category.name.trim(), category.colorArgb)
        return if (category.id == 0L) categoryDao.insert(entity) else entity.id.also { categoryDao.update(entity) }
    }

    override suspend fun deleteCategory(id: Long) = categoryDao.delete(id, requireUserId())

    override suspend fun isCategoryNameTaken(name: String, excludeId: Long): Boolean =
        categoryDao.countWithName(requireUserId(), name.trim(), excludeId) > 0

    private suspend fun requireUserId(): Long =
        settings.sessionUserId.first() ?: throw IllegalStateException("Nenhum usuário logado")

    private fun TaskWithSubtasks.toDomain() = Task(
        id = task.id,
        title = task.title,
        description = task.description,
        dueDate = task.dueDate,
        dueTime = task.dueTime,
        categoryId = task.categoryId,
        manualUrgency = task.manualUrgency,
        isDone = task.isDone,
        completedAt = task.completedAt,
        subtasks = subtasks.sortedBy { it.position }.map { Subtask(it.id, it.title, it.isDone) },
    )

    private fun CategoryEntity.toDomain() = Category(id, name, colorArgb)
}
