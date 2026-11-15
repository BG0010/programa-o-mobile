package com.todolist.app.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.todolist.app.data.repository.TaskRepository
import com.todolist.app.domain.model.Category
import com.todolist.app.ui.BaseViewModel
import com.todolist.app.ui.CollectMessages
import com.todolist.app.ui.appContainer
import com.todolist.app.ui.components.AppCard
import com.todolist.app.ui.components.ConfirmDialog
import com.todolist.app.ui.components.EmptyState
import com.todolist.app.ui.components.color
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Paleta disponível para as categorias, com nomes para leitores de tela. */
val CategoryPalette = listOf(
    0xFF7C5CE0 to "Roxo", 0xFF3B6FE0 to "Azul", 0xFFD9487A to "Rosa", 0xFF2E9E6B to "Verde",
    0xFFE07B12 to "Laranja", 0xFF0EA5B7 to "Ciano", 0xFFDC2626 to "Vermelho", 0xFF6B7489 to "Cinza",
)
const val MAX_CATEGORY_NAME = 20

data class CategoryUi(val category: Category, val taskCount: Int)

class CategoriesViewModel(private val repository: TaskRepository) : BaseViewModel() {
    val categories: StateFlow<List<CategoryUi>?> = combine(repository.categories, repository.tasks) { categories, tasks ->
        categories.map { c -> CategoryUi(c, tasks.count { it.categoryId == c.id }) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Valida e salva. Retorna a mensagem de erro do campo nome, ou null em caso de sucesso. */
    suspend fun validate(id: Long, name: String): String? = when {
        name.isBlank() -> "Informe o nome da categoria."
        runCatching { repository.isCategoryNameTaken(name, id) }.getOrDefault(false) -> "Já existe uma categoria com este nome."
        else -> null
    }

    fun save(id: Long, name: String, color: Long) = launchSafely(
        errorMessage = "Não foi possível salvar a categoria.",
        successMessage = if (id == 0L) "Categoria criada" else "Categoria atualizada",
    ) { repository.saveCategory(Category(id, name, color)) }

    fun delete(category: Category) = launchSafely(
        errorMessage = "Não foi possível excluir a categoria.",
        successMessage = "Categoria excluída",
    ) { repository.deleteCategory(category.id) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(onBack: () -> Unit) {
    val container = appContainer()
    val viewModel: CategoriesViewModel = viewModel { CategoriesViewModel(container.taskRepository) }
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    CollectMessages(viewModel)

    // id 0 = nova categoria; null = diálogo fechado.
    var editing by remember { mutableStateOf<Category?>(null) }
    var pendingDelete by remember { mutableStateOf<CategoryUi?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorias") },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = Category(0, "", CategoryPalette.first().first) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nova categoria") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { padding ->
        val list = categories
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (list != null && list.isEmpty()) item { EmptyState("Nenhuma categoria. Toque em \"Nova categoria\".") }
            items(list.orEmpty(), key = { it.category.id }) { item ->
                AppCard(Modifier.fillMaxWidth(), contentPadding = 12) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(14.dp).clip(CircleShape).background(item.category.color()))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.category.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                if (item.taskCount == 1) "1 tarefa" else "${item.taskCount} tarefas",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { editing = item.category }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Editar ${item.category.name}")
                        }
                        IconButton(onClick = { pendingDelete = item }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Excluir ${item.category.name}",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }

    editing?.let { category ->
        CategoryDialog(
            initial = category,
            validate = viewModel::validate,
            onSave = { name, color ->
                viewModel.save(category.id, name, color)
                editing = null
            },
            onDismiss = { editing = null },
        )
    }

    pendingDelete?.let { item ->
        ConfirmDialog(
            title = "Excluir categoria?",
            message = if (item.taskCount > 0) {
                "\"${item.category.name}\" será excluída. ${item.taskCount} tarefa(s) ficarão sem categoria."
            } else {
                "\"${item.category.name}\" será excluída."
            },
            confirmText = "Excluir",
            onConfirm = {
                viewModel.delete(item.category)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun CategoryDialog(
    initial: Category,
    validate: suspend (Long, String) -> String?,
    onSave: (String, Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var color by rememberSaveable { mutableLongStateOf(initial.colorArgb) }
    var error by remember { mutableStateOf<String?>(null) }
    var submit by remember { mutableStateOf(0) }

    // Valida (inclusive nome duplicado no banco) quando o usuário toca em Salvar.
    androidx.compose.runtime.LaunchedEffect(submit) {
        if (submit == 0) return@LaunchedEffect
        error = validate(initial.id, name)
        if (error == null) onSave(name.trim(), color)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == 0L) "Nova categoria" else "Editar categoria") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(MAX_CATEGORY_NAME)
                        error = null
                    },
                    label = { Text("Nome") },
                    isError = error != null,
                    supportingText = { Text(error ?: "${name.length}/$MAX_CATEGORY_NAME") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text("Cor", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CategoryPalette.forEach { (argb, label) ->
                        val selected = argb == color
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(argb))
                                .then(
                                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier,
                                )
                                .clickable { color = argb }
                                .semantics {
                                    contentDescription = label
                                    this.selected = selected
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { submit++ }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
