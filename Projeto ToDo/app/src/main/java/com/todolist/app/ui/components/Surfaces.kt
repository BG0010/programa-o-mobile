package com.todolist.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Cartão com borda suave, usado em todas as telas. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: Int = 16,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val border = BorderStroke(1.dp, borderColor)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = containerColor, border = border) {
            Column(Modifier.padding(contentPadding.dp), content = content)
        }
    } else {
        Surface(modifier = modifier, shape = shape, color = containerColor, border = border) {
            Column(Modifier.padding(contentPadding.dp), content = content)
        }
    }
}

/** Cabeçalho das telas principais (título + subtítulo). */
@Composable
fun ScreenHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

/** Ícone dentro de um quadrado arredondado colorido. */
@Composable
fun IconTile(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    container: Color = tint.copy(alpha = 0.12f),
    size: Int = 36,
) {
    Box(
        modifier = modifier.size(size.dp).clip(RoundedCornerShape(10.dp)).background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size * 0.55f).dp))
    }
}

/** Linha de informação: ícone + rótulo + valor + conteúdo à direita. */
@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
        trailing?.invoke()
    }
}

/** Estado vazio com mensagem. */
@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
    )
}
