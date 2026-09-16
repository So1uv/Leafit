package com.example.fitnesstracker.ui.screens.appsettings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.LeafIcons
import com.example.fitnesstracker.ui.components.LeafRosette
import com.example.fitnesstracker.ui.theme.LeafSurface

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AppVersionCard() {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val logoShape = remember { LeafRosette(8) }
    val info = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0) }.getOrNull()
    }
    Column(Modifier.fillMaxWidth().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.settings_about_section),
            Modifier.padding(horizontal = 4.dp), style = MaterialTheme.typography.titleSmall,
            color = colors.onSurfaceVariant)
        LeafSurface(shape = RoundedCornerShape(20.dp), color = colors.surfaceContainerLow) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LeafSurface(shape = logoShape, color = colors.primaryContainer,
                        contentColor = colors.onPrimaryContainer) {
                        Box(Modifier.size(64.dp), Alignment.Center) {
                            Icon(LeafIcons.Exercise, null, Modifier.size(32.dp))
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.release_app_description),
                            style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LeafSurface(shape = CircleShape, color = colors.tertiaryContainer,
                        contentColor = colors.onTertiaryContainer) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.settings_version), style = MaterialTheme.typography.labelMedium)
                            Text(info?.versionName ?: "—", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                    info?.let { packageInfo ->
                        LeafSurface(shape = CircleShape, color = colors.surfaceContainerHighest,
                            contentColor = colors.onSurfaceVariant) {
                            Text(stringResource(R.string.release_build, PackageInfoCompat.getLongVersionCode(packageInfo)),
                                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Text(stringResource(R.string.release_description), style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant)
            }
        }
    }
}
