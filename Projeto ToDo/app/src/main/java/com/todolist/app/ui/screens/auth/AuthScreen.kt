package com.todolist.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.todolist.app.ui.appContainer
import com.todolist.app.ui.components.AppCard
import com.todolist.app.ui.theme.Blue500
import com.todolist.app.ui.theme.Blue600

/** Tela única que alterna entre "Entrar" e "Criar conta". */
@Composable
fun AuthScreen() {
    val container = appContainer()
    val viewModel: AuthViewModel = viewModel { AuthViewModel(container.authRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isRegister by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current

    val submit = {
        focus.clearFocus()
        if (isRegister) viewModel.register() else viewModel.login()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))
            Box(
                Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(Blue500, Blue600))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("TodoList", style = MaterialTheme.typography.headlineMedium)
            Text(
                if (isRegister) "Crie sua conta para salvar suas tarefas" else "Entre para ver suas tarefas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))

            AppCard(Modifier.fillMaxWidth(), contentPadding = 20) {
                Text(if (isRegister) "Criar conta" else "Entrar", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isRegister) {
                        AuthField(
                            value = state.name, onValueChange = viewModel::onNameChange,
                            label = "Nome", icon = Icons.Outlined.Person, error = state.nameError,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        )
                    }
                    AuthField(
                        value = state.email, onValueChange = viewModel::onEmailChange,
                        label = "E-mail", icon = Icons.Outlined.Email, error = state.emailError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    )
                    PasswordField(
                        value = state.password, onValueChange = viewModel::onPasswordChange,
                        label = "Senha", error = state.passwordError,
                        imeAction = if (isRegister) ImeAction.Next else ImeAction.Done,
                        onDone = submit,
                    )
                    if (isRegister) {
                        PasswordField(
                            value = state.confirmPassword, onValueChange = viewModel::onConfirmChange,
                            label = "Confirmar senha", error = state.confirmError,
                            imeAction = ImeAction.Done, onDone = submit,
                        )
                    }
                }
                state.formError?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                            .padding(10.dp),
                    )
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = submit,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(if (isRegister) "Criar conta" else "Entrar")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isRegister) "Já tem uma conta?" else "Ainda não tem conta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = {
                    isRegister = !isRegister
                    viewModel.reset()
                }) {
                    Text(if (isRegister) "Entrar" else "Criar conta")
                }
            }
            Text(
                "Sua conta e suas tarefas ficam salvas apenas neste aparelho.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String?,
    keyboardOptions: KeyboardOptions,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(14.dp),
        colors = authFieldColors(),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    imeAction: ImeAction,
    onDone: () -> Unit,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Ocultar senha" else "Mostrar senha",
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        shape = RoundedCornerShape(14.dp),
        colors = authFieldColors(),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
)
