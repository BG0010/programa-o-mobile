package com.todolist.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface UserDao {
    /** Falha com SQLiteConstraintException se o e-mail já existir (índice único). */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observe(id: Long): Flow<UserEntity?>

    @Query("UPDATE users SET name = :name WHERE id = :id")
    suspend fun updateName(id: Long, name: String)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY id")
    fun observeAll(userId: Long): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id AND userId = :userId")
    suspend fun delete(id: Long, userId: Long)

    @Query(
        "SELECT COUNT(*) FROM categories WHERE userId = :userId AND LOWER(name) = LOWER(:name) AND id != :excludeId",
    )
    suspend fun countWithName(userId: Long, name: String, excludeId: Long): Int
}

/** Todas as consultas filtram por userId: um usuário nunca acessa tarefas de outro. */
@Dao
abstract class TaskDao {
    @Transaction
    @Query("SELECT * FROM tasks WHERE userId = :userId")
    abstract fun observeAll(userId: Long): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id AND userId = :userId")
    abstract fun observe(id: Long, userId: Long): Flow<TaskWithSubtasks?>

    @Insert
    protected abstract suspend fun insertTask(task: TaskEntity): Long

    @Update
    protected abstract suspend fun updateTask(task: TaskEntity): Int

    @Insert
    protected abstract suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    protected abstract suspend fun deleteSubtasksOf(taskId: Long)

    @Query("SELECT COUNT(*) FROM tasks WHERE id = :id AND userId = :userId")
    protected abstract suspend fun countOwned(id: Long, userId: Long): Int

    /** Cria ou atualiza a tarefa e substitui suas subtarefas, tudo na mesma transação. */
    @Transaction
    open suspend fun save(task: TaskEntity, subtasks: List<SubtaskEntity>): Long {
        val taskId = if (task.id == 0L) {
            insertTask(task)
        } else {
            check(countOwned(task.id, task.userId) == 1) { "Tarefa não encontrada" }
            updateTask(task)
            deleteSubtasksOf(task.id)
            task.id
        }
        insertSubtasks(subtasks.mapIndexed { i, s -> s.copy(id = 0, taskId = taskId, position = i) })
        return taskId
    }

    @Query("DELETE FROM tasks WHERE id = :id AND userId = :userId")
    abstract suspend fun delete(id: Long, userId: Long)

    @Query("UPDATE tasks SET isDone = :done, completedAt = :completedAt WHERE id = :id AND userId = :userId")
    abstract suspend fun setDone(id: Long, userId: Long, done: Boolean, completedAt: LocalDate?)

    @Query(
        """UPDATE subtasks SET isDone = :done
           WHERE id = :subtaskId AND taskId IN (SELECT id FROM tasks WHERE id = :taskId AND userId = :userId)""",
    )
    abstract suspend fun setSubtaskDone(taskId: Long, subtaskId: Long, userId: Long, done: Boolean)
}
