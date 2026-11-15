package com.todolist.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.todolist.app.domain.model.User
import com.todolist.app.ui.AppSnackbar
import com.todolist.app.ui.LocalAppSnackbar
import com.todolist.app.ui.screens.calendar.CalendarScreen
import com.todolist.app.ui.screens.categories.CategoriesScreen
import com.todolist.app.ui.screens.detail.TaskDetailScreen
import com.todolist.app.ui.screens.form.TaskFormScreen
import com.todolist.app.ui.screens.settings.SettingsScreen
import com.todolist.app.ui.screens.statistics.StatisticsScreen
import com.todolist.app.ui.screens.tasks.TasksScreen

object Routes {
    const val TASKS = "tasks"
    const val CALENDAR = "calendar"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val NEW_TASK = "task/new"
    const val TASK_DETAIL = "task/{taskId}"
    const val EDIT_TASK = "task/{taskId}/edit"
    const val CATEGORIES = "categories"

    fun taskDetail(id: Long) = "task/$id"
    fun editTask(id: Long) = "task/$id/edit"
}

private enum class TopLevel(val route: String, val label: String, val icon: ImageVector) {
    TASKS(Routes.TASKS, "Tarefas", Icons.Outlined.Checklist),
    CALENDAR(Routes.CALENDAR, "Calendário", Icons.Outlined.CalendarMonth),
    STATISTICS(Routes.STATISTICS, "Estatísticas", Icons.Outlined.BarChart),
    SETTINGS(Routes.SETTINGS, "Ajustes", Icons.Outlined.Settings),
}

@Composable
fun TodoNavHost(
    user: User,
    navController: NavHostController = rememberNavController(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val appSnackbar = remember { AppSnackbar(snackbarHostState, scope) }
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val isTopLevel = TopLevel.entries.any { top -> destination?.hierarchy?.any { it.route == top.route } == true }

    CompositionLocalProvider(LocalAppSnackbar provides appSnackbar) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { if (isTopLevel) BottomBar(navController, destination?.route) },
        floatingActionButton = {
            if (destination?.route == Routes.TASKS) {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.NEW_TASK) },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Nova tarefa")
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TASKS,
            // Telas de nível superior usam o padding da barra inferior; telas internas têm o próprio Scaffold.
            modifier = if (isTopLevel) Modifier.padding(bottom = padding.calculateBottomPadding()) else Modifier,
        ) {
            val openTask: (Long) -> Unit = { navController.navigate(Routes.taskDetail(it)) }
            composable(Routes.TASKS) { TasksScreen(onOpenTask = openTask, userName = user.name) }
            composable(Routes.CALENDAR) { CalendarScreen(onOpenTask = openTask) }
            composable(Routes.STATISTICS) { StatisticsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(user = user, onOpenCategories = { navController.navigate(Routes.CATEGORIES) })
            }
            composable(Routes.CATEGORIES) { CategoriesScreen(onBack = navController::popBackStack) }
            composable(Routes.NEW_TASK) { TaskFormScreen(taskId = null, onBack = navController::popBackStack) }
            composable(
                Routes.TASK_DETAIL,
                arguments = listOf(navArgument("taskId") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("taskId") ?: return@composable
                TaskDetailScreen(
                    taskId = id,
                    onBack = navController::popBackStack,
                    onEdit = { navController.navigate(Routes.editTask(it)) },
                )
            }
            composable(
                Routes.EDIT_TASK,
                arguments = listOf(navArgument("taskId") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("taskId") ?: return@composable
                TaskFormScreen(taskId = id, onBack = navController::popBackStack)
            }
        }
    }
    }
}

@Composable
private fun BottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        TopLevel.entries.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}
