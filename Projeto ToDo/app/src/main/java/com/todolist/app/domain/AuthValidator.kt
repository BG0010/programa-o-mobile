package com.todolist.app.domain

/** Regras de validação dos formulários de login e cadastro (sem dependência de Android). */
object AuthValidator {
    const val MIN_PASSWORD = 6
    private val EmailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun normalizeEmail(email: String): String = email.trim().lowercase()

    fun nameError(name: String): String? = when {
        name.isBlank() -> "Informe seu nome."
        name.trim().length < 2 -> "O nome precisa ter pelo menos 2 letras."
        else -> null
    }

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Informe seu e-mail."
        !EmailRegex.matches(email.trim()) -> "E-mail inválido."
        else -> null
    }

    fun passwordError(password: String): String? = when {
        password.isEmpty() -> "Informe sua senha."
        password.length < MIN_PASSWORD -> "A senha precisa ter pelo menos $MIN_PASSWORD caracteres."
        else -> null
    }

    fun confirmError(password: String, confirm: String): String? =
        if (password != confirm) "As senhas não conferem." else null
}
