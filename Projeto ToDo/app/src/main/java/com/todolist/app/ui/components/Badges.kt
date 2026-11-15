package com.todolist.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todolist.app.domain.model.Category
import com.todolist.app.domain.model.Urgency
import com.todolist.app.ui.theme.AttentionOrange
import com.todolist.app.ui.theme.AttentionOrangeBg
import com.todolist.app.ui.theme.CalmGreen
import com.todolist.app.ui.theme.CalmGreenBg
import com.todolist.app.ui.theme.OverdueWine
import com.todolist.app.ui.theme.OverdueWineBg
import com.todolist.app.ui.theme.UrgentRed
import com.todolist.app.ui.theme.UrgentRedBg

data class UrgencyColors(val content: Color, val container: Color)

/** Cores do selo; no tema black usam tons mais claros e fundo translúcido para manter o contraste. */
@Composable
@ReadOnlyComposable
fun Urgency.colors(): UrgencyColors {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (dark) {
        val content = when (this) {
            Urgency.OVERDUE -> Color(0xFFFF8A94)
            Urgency.URGENT -> Color(0xFFFF6B6F)
            Urgency.ATTENTION -> Color(0xFFFFB454)
            Urgency.CALM -> Color(0xFF4ADE80)
        }
        UrgencyColors(content, content.copy(alpha = 0.16f))
    } else {
        when (this) {
            Urgency.OVERDUE -> UrgencyColors(OverdueWine, OverdueWineBg)
            Urgency.URGENT -> UrgencyColors(UrgentRed, UrgentRedBg)
            Urgency.ATTENTION -> UrgencyColors(AttentionOrange, AttentionOrangeBg)
            Urgency.CALM -> UrgencyColors(CalmGreen, CalmGreenBg)
        }
    }
}

/** Selo de urgência: ponto + texto (não depende só da cor). */
@Composable
fun UrgencyBadge(urgency: Urgency, modifier: Modifier = Modifier) {
    val colors = urgency.colors()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(colors.container)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(colors.content))
        Text(urgency.label, style = MaterialTheme.typography.labelSmall, color = colors.content)
    }
}

fun Category.color(): Color = Color(colorArgb)

@Composable
fun CategoryChip(category: Category, modifier: Modifier = Modifier) {
    Pill(
        text = category.name,
        content = category.color(),
        container = category.color().copy(alpha = 0.12f),
        modifier = modifier,
    )
}

/** Pequena etiqueta colorida genérica (ex.: "Vence amanhã"). */
@Composable
fun Pill(text: String, content: Color, container: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = content,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(container)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
