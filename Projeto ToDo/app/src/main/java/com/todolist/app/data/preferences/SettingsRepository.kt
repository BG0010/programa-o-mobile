package com.todolist.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    /** Tema black ativado por padrão (pedido da equipe). */
    val darkMode: Boolean = true,
    val notifications: Boolean = true,
    val autoUrgency: Boolean = true,
)

/** Preferências simples do aparelho e a sessão (id do usuário logado). */
class SettingsRepository(context: Context) {
    private val store = context.applicationContext.dataStore

    private val data: Flow<Preferences> = store.data.catch { e ->
        // Arquivo corrompido ou ilegível: segue com valores padrão em vez de travar o app.
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    val sessionUserId: Flow<Long?> = data.map { it[SESSION_USER_ID] }.distinctUntilChanged()

    val settings: Flow<AppSettings> = data.map {
        AppSettings(
            darkMode = it[DARK_MODE] ?: true,
            notifications = it[NOTIFICATIONS] ?: true,
            autoUrgency = it[AUTO_URGENCY] ?: true,
        )
    }.distinctUntilChanged()

    suspend fun setSession(userId: Long?) = store.edit {
        if (userId == null) it.remove(SESSION_USER_ID) else it[SESSION_USER_ID] = userId
    }

    suspend fun setDarkMode(value: Boolean) = store.edit { it[DARK_MODE] = value }
    suspend fun setNotifications(value: Boolean) = store.edit { it[NOTIFICATIONS] = value }
    suspend fun setAutoUrgency(value: Boolean) = store.edit { it[AUTO_URGENCY] = value }

    private companion object {
        val SESSION_USER_ID = longPreferencesKey("session_user_id")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val AUTO_URGENCY = booleanPreferencesKey("auto_urgency")
    }
}
