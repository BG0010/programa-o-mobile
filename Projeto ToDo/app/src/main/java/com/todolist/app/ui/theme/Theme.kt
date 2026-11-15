package com.todolist.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue600,
    secondary = Slate500,
    secondaryContainer = Blue50,
    onSecondaryContainer = Blue600,
    surfaceContainerHighest = Slate200,
    background = Background,
    onBackground = Navy900,
    surface = Color.White,
    onSurface = Navy900,
    surfaceVariant = Background,
    onSurfaceVariant = Slate500,
    outline = Slate200,
    outlineVariant = Slate200,
    error = UrgentRed,
)

/** Tema black (padrão do app). */
private val BlackColors = darkColorScheme(
    primary = Blue400,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF14254A),
    onPrimaryContainer = Color(0xFFBFD3FF),
    secondary = Color(0xFFA0A0A0),
    secondaryContainer = Color(0xFF14254A),
    onSecondaryContainer = Color(0xFFBFD3FF),
    surfaceContainerHighest = OutlineBlack,
    surfaceContainer = SurfaceBlack,
    background = Black,
    onBackground = Color(0xFFEDEDED),
    surface = SurfaceBlack,
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = SurfaceBlackVariant,
    onSurfaceVariant = Color(0xFFA0A0A0),
    outline = OutlineBlack,
    outlineVariant = OutlineBlack,
    error = Color(0xFFFF6B6F),
)

@Composable
fun TodoListTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) BlackColors else LightColors,
        typography = Typography,
        content = content,
    )
}
