package com.todolist.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.todolist.app.data.preferences.AppSettings
import com.todolist.app.data.preferences.SettingsRepository
import com.todolist.app.data.repository.AuthRepository
import com.todolist.app.domain.AuthValidator
import com.todolist.app.domain.model.User
import com.todolist.app.ui.BaseViewModel
import com.todolist.app.ui.CollectMessages
import com.todolist.app.ui.appContainer
import com.todolist.app.ui.components.AppCard
import com.todolist.app.ui.components.ConfirmDialog
import com.todolist.app.ui.components.IconTile
import com.todolist.app.ui.components.ScreenHeader
import com.todolist.app.ui.theme.AttentionOrange
import com.todolist.app.ui.theme.Blue500
import com.todolist.app.ui.theme.Blue600
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private val Creators = listOf(
    "Guilherme Oliveira", "Kaue Bueno", "Pedro Henrique Gomes",
    "Vinicius Borges", "Lucas Cardoso", "Henrique Gomes",
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {
    val settings: StateFlow<AppSettings> =
        settingsRepository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setDarkMode(v: Boolean) = launchSafely("Não foi possível salvar a preferência.") { settingsRepository.setDarkMode(v) }
    fun setAutoUrgency(v: Boolean) = launchSafely("Não foi possível salvar a preferência.") { settingsRepository.setAutoUrgency(v) }

    fun updateName(name: String) = launchSafely(
        errorMessage = "Não foi possível atualizar o nome.",
        successMessage = "Nome atualizado",
    ) { authRepository.updateName(name) }

    fun logout() = launchSafely("Não foi possível sair da conta.") { authRepository.logout() }
}

@Composable
fun SettingsScreen(user: User, onOpenCategories: () -> Unit) {
    val container = appContainer()
    val viewModel: SettingsViewModel = viewModel {
        SettingsViewModel(container.authRepository, container.settingsRepository)
    }
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var editingName by rememberSaveable { mutableStateOf(false) }
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    CollectMessages(viewModel)

    if (editingName) {
        EditNameDialog(
            initial = user.name,
            onSave = {
                viewModel.updateName(it)
                editingName = false
            },
            onDismiss = { editingName = false },
        )
    }
    if (confirmLogout) {
        ConfirmDialog(
            title = "Sair da conta?",
            message = "Suas tarefas continuam salvas neste aparelho e aparecem quando você entrar novamente.",
            confirmText = "Sair",
            onConfirm = {
                confirmLogout = false
                viewModel.logout()
            },
            onDismiss = { confirmLogout = false },
        )
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Configurações", "Deixe o app do seu jeito")
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProfileCard(user, onEdit = { editingName = true })

            SectionLabel("Preferências")
            AppCard(Modifier.fillMaxWidth(), contentPadding = 4) {
                SwitchRow(
                    Icons.Outlined.DarkMode, MaterialTheme.colorScheme.primary,
                    "Tema black", "Fundo preto, mais conforto à noite",
                    checked = settings.darkMode, onCheckedChange = viewModel::setDarkMode,
                )
                RowDivider()
                SwitchRow(
                    Icons.Outlined.NotificationsNone, Color(0xFF7C5CE0),
                    "Notificações", "Lembretes dos prazos — disponível em breve",
                    checked = false, enabled = false, onCheckedChange = {},
                )
                RowDivider()
                SwitchRow(
                    Icons.Outlined.Flag, AttentionOrange,
                    "Urgência automática", "Novas tarefas recebem o selo pela data de entrega",
                    checked = settings.autoUrgency, onCheckedChange = viewModel::setAutoUrgency,
                )
            }

            SectionLabel("Organização")
            AppCard(Modifier.fillMaxWidth(), contentPadding = 4) {
                NavRow(Icons.Outlined.Category, Color(0xFF2E9E6B), "Categorias", "Criar, editar e excluir categorias", onOpenCategories)
            }

            SectionLabel("Conta")
            AppCard(Modifier.fillMaxWidth(), contentPadding = 4) {
                NavRow(
                    Icons.AutoMirrored.Outlined.Logout, MaterialTheme.colorScheme.error,
                    "Sair da conta", "Voltar para a tela de login",
                    onClick = { confirmLogout = true },
                )
            }

            SectionLabel("Sobre")
            AppCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Outlined.Info, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("TodoList", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Programação Mobile · UNASP-SP · 2026/2",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Criadores", style = MaterialTheme.typography.labelMedium)
                Text(
                    Creators.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "TodoList · versão 1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun ProfileCard(user: User, onEdit: () -> Unit) {
    val initials = user.name.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
    AppCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(listOf(Blue500, Blue600))),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials, style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(user.name, style = MaterialTheme.typography.titleSmall)
                Text(user.email, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalIconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = "Editar nome")
            }
        }
    }
}

@Composable
private fun EditNameDialog(initial: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar nome") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it.take(40)
                    error = null
                },
                label = { Text("Nome") },
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
        },
        confirmButton = {
            TextButton(onClick = {
                error = AuthValidator.nameError(name)
                if (error == null) onSave(name)
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 2.dp),
    )
}

@Composable
private fun RowDivider() = HorizontalDivider(Modifier.padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.outline)

@Composable
private fun SwitchRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    // A linha inteira alterna o interruptor (área de toque maior e acessível).
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon, tint = tint)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}

@Composable
private fun NavRow(icon: ImageVector, tint: Color, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon, tint = tint)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
