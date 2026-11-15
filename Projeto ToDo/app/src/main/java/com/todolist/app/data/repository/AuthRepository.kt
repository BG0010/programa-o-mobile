package com.todolist.app.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.todolist.app.data.local.AppDatabase
import com.todolist.app.data.local.CategoryEntity
import com.todolist.app.data.local.UserEntity
import com.todolist.app.data.preferences.SettingsRepository
import com.todolist.app.data.security.PasswordHasher
import com.todolist.app.domain.AuthValidator
import com.todolist.app.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data class Error(val message: String) : AuthResult
}

/**
 * Contas locais: os usuários ficam salvos apenas neste aparelho (sem servidor).
 * Cada tarefa e categoria pertence a um usuário.
 */
class AuthRepository(
    private val db: AppDatabase,
    private val settings: SettingsRepository,
) {
    private val userDao = db.userDao()

    /** Usuário logado ou null. Se a conta salva na sessão não existir mais, retorna null. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: Flow<User?> = settings.sessionUserId.flatMapLatest { id ->
        if (id == null) flowOf(null) else userDao.observe(id).map { it?.toDomain() }
    }

    suspend fun register(name: String, email: String, password: String): AuthResult {
        val normalized = AuthValidator.normalizeEmail(email)
        return try {
            val user = withContext(Dispatchers.Default) { PasswordHasher.hash(password) }.let { hashed ->
                db.withTransaction {
                    val entity = UserEntity(
                        name = name.trim(),
                        email = normalized,
                        passwordHash = hashed.hash,
                        salt = hashed.salt,
                        createdAt = System.currentTimeMillis(),
                    )
                    val id = userDao.insert(entity)
                    db.categoryDao().insertAll(DefaultCategories.map { (n, c) -> CategoryEntity(userId = id, name = n, colorArgb = c) })
                    entity.copy(id = id)
                }
            }
            settings.setSession(user.id)
            AuthResult.Success(user.toDomain())
        } catch (_: SQLiteConstraintException) {
            AuthResult.Error("Já existe uma conta com este e-mail.")
        } catch (_: Exception) {
            AuthResult.Error("Não foi possível criar a conta. Tente novamente.")
        }
    }

    suspend fun login(email: String, password: String): AuthResult = try {
        val user = userDao.findByEmail(AuthValidator.normalizeEmail(email))
        val valid = user != null && withContext(Dispatchers.Default) {
            PasswordHasher.verify(password, user.passwordHash, user.salt)
        }
        if (user != null && valid) {
            settings.setSession(user.id)
            AuthResult.Success(user.toDomain())
        } else {
            // Mensagem única para não revelar se o e-mail existe.
            AuthResult.Error("E-mail ou senha incorretos.")
        }
    } catch (_: Exception) {
        AuthResult.Error("Não foi possível entrar. Tente novamente.")
    }

    suspend fun logout() = settings.setSession(null)

    suspend fun updateName(name: String) {
        val id = settings.sessionUserId.first() ?: return
        userDao.updateName(id, name.trim())
    }

    private fun UserEntity.toDomain() = User(id, name, email)

    companion object {
        /** Categorias criadas automaticamente para cada nova conta (as mesmas do protótipo). */
        val DefaultCategories = listOf(
            "Estudos" to 0xFF7C5CE0,
            "Trabalho" to 0xFF3B6FE0,
            "Pessoal" to 0xFFD9487A,
            "Casa" to 0xFF2E9E6B,
        )
    }
}
