package com.todolist.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

private val Base = Typography()

val Typography = Typography(
    headlineMedium = Base.headlineMedium.copy(fontWeight = FontWeight.Medium),
    headlineSmall = Base.headlineSmall.copy(fontWeight = FontWeight.Medium),
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.Medium),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = Base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = Base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
)
