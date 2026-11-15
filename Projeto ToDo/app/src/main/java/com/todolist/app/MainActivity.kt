package com.todolist.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.todolist.app.data.preferences.AppSettings
import com.todolist.app.data.preferences.SettingsRepository
import com.todolist.app.data.repository.AuthRepository
import com.todolist.app.domain.model.User
import com.todolist.app.ui.appContainer
import com.todolist.app.ui.navigation.TodoNavHost
import com.todolist.app.ui.screens.auth.AuthScreen
import com.todolist.app.ui.theme.TodoListTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val user: User) : SessionState
}

class MainViewModel(auth: AuthRepository, settings: SettingsRepository) : ViewModel() {
    val session: StateFlow<SessionState> = auth.currentUser
        .map { user -> if (user == null) SessionState.LoggedOut else SessionState.LoggedIn(user) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    val settings: StateFlow<AppSettings> = settings.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TodoListRoot() }
    }
}

/** Escolhe entre login e app conforme a sessão salva, aplicando o tema escolhido. */
@Composable
private fun TodoListRoot() {
    val container = appContainer()
    val viewModel: MainViewModel = viewModel { MainViewModel(container.authRepository, container.settingsRepository) }
    val session by viewModel.session.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // Ícones da barra de status claros no tema black e escuros no tema claro.
    val activity = LocalActivity.current as? ComponentActivity
    LaunchedEffect(settings.darkMode) {
        val transparent = android.graphics.Color.TRANSPARENT
        val style = if (settings.darkMode) SystemBarStyle.dark(transparent) else SystemBarStyle.light(transparent, transparent)
        activity?.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }

    TodoListTheme(darkTheme = settings.darkMode) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (val s = session) {
                SessionState.Loading -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
                SessionState.LoggedOut -> AuthScreen()
                // key: ao trocar de usuário, toda a navegação (e seus ViewModels) é recriada.
                is SessionState.LoggedIn -> androidx.compose.runtime.key(s.user.id) { TodoNavHost(user = s.user) }
            }
        }
    }
}
