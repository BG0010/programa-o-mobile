package com.todolist.app.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todolist.app.AppContainer
import com.todolist.app.TodoListApplication
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Acesso ao container de dependências a partir de um Composable. */
@Composable
fun appContainer(): AppContainer =
    (LocalContext.current.applicationContext as TodoListApplication).container

/**
 * ViewModel base: executa operações de dados capturando erros
 * e enviando mensagens curtas para a interface (Snackbar), sem derrubar o app.
 */
abstract class BaseViewModel : ViewModel() {
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    protected fun showMessage(message: String) {
        _messages.trySend(message)
    }

    protected fun launchSafely(
        errorMessage: String,
        successMessage: String? = null,
        onSuccess: () -> Unit = {},
        block: suspend () -> Unit,
    ) = viewModelScope.launch {
        try {
            block()
            successMessage?.let(::showMessage)
            onSuccess()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            showMessage(errorMessage)
        }
    }
}

/** Snackbar global, com escopo próprio para a mensagem sobreviver à troca de tela. */
class AppSnackbar(val hostState: SnackbarHostState, private val scope: CoroutineScope) {
    fun show(message: String) {
        scope.launch {
            hostState.currentSnackbarData?.dismiss()
            hostState.showSnackbar(message)
        }
    }
}

val LocalAppSnackbar = staticCompositionLocalOf<AppSnackbar> { error("AppSnackbar não fornecido") }

/** Exibe no Snackbar global as mensagens emitidas por um ViewModel. */
@Composable
fun CollectMessages(viewModel: BaseViewModel) {
    val snackbar = LocalAppSnackbar.current
    LaunchedEffect(viewModel) { viewModel.messages.collect(snackbar::show) }
}
