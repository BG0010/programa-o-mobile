package com.todolist.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todolist.app.data.repository.AuthRepository
import com.todolist.app.data.repository.AuthResult
import com.todolist.app.domain.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    /** Erro vindo do repositório (credenciais, e-mail duplicado, falha no banco). */
    val formError: String? = null,
    val isLoading: Boolean = false,
)

/**
 * Login e cadastro. Ao concluir com sucesso, a sessão é salva no DataStore
 * e a MainActivity troca automaticamente para as telas do app.
 */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthFormState())
    val state: StateFlow<AuthFormState> = _state.asStateFlow()

    fun onNameChange(v: String) = _state.update { it.copy(name = v, nameError = null, formError = null) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = v, emailError = null, formError = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, passwordError = null, formError = null) }
    fun onConfirmChange(v: String) = _state.update { it.copy(confirmPassword = v, confirmError = null, formError = null) }

    /** Limpa erros e senhas ao alternar entre login e cadastro. */
    fun reset() = _state.update { AuthFormState(email = it.email) }

    fun login() {
        val s = _state.value
        val emailError = AuthValidator.emailError(s.email)
        val passwordError = if (s.password.isEmpty()) "Informe sua senha." else null
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        submit { authRepository.login(s.email, s.password) }
    }

    fun register() {
        val s = _state.value
        val errors = s.copy(
            nameError = AuthValidator.nameError(s.name),
            emailError = AuthValidator.emailError(s.email),
            passwordError = AuthValidator.passwordError(s.password),
            confirmError = AuthValidator.confirmError(s.password, s.confirmPassword),
        )
        if (listOf(errors.nameError, errors.emailError, errors.passwordError, errors.confirmError).any { it != null }) {
            _state.value = errors
            return
        }
        submit { authRepository.register(s.name, s.email, s.password) }
    }

    private fun submit(action: suspend () -> AuthResult) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            when (val result = action()) {
                is AuthResult.Success -> _state.update { it.copy(isLoading = false, password = "", confirmPassword = "") }
                is AuthResult.Error -> _state.update { it.copy(isLoading = false, formError = result.message) }
            }
        }
    }
}
