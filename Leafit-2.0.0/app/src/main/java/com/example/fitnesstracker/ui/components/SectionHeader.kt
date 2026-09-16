package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R

@Composable
fun appHeaderHeight(): Dp = 64.dp + 24.dp * (LocalDensity.current.fontScale - 1f).coerceAtLeast(0f)

@Composable
fun SectionHeader(title: String, subtitle: String, icon: ImageVector,
    modifier: Modifier = Modifier, actions: (@Composable RowScope.() -> Unit)? = null,
    headerState: LeafHeaderState? = null) {
    Row(modifier.fillMaxWidth().height(appHeaderHeight()), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f).leafHeaderMotion(headerState), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            LeafTitle(title, style = MaterialTheme.typography.headlineSmall, maxLines = 1)
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
        actions?.invoke(this)
    }
}

@Composable
fun CompactPageTopBar(title: String, subtitle: String, icon: ImageVector,
    onBack: (() -> Unit)? = null, headerState: LeafHeaderState? = null) {
    Row(Modifier.fillMaxWidth().leafHeaderScrim(MaterialTheme.colorScheme.surface, headerState)
        .statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (onBack != null) FilledTonalIconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(LeafIcons.ArrowBack, stringResource(R.string.common_back))
        }
        SectionHeader(title, subtitle, icon, Modifier.weight(1f), headerState = headerState)
    }
}
