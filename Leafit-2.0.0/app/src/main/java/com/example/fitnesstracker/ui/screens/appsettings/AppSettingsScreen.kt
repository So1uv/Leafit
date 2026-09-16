package com.example.fitnesstracker.ui.screens.appsettings

import com.example.fitnesstracker.ui.components.LeafScreenScaffold
import com.example.fitnesstracker.ui.components.rememberLeafHeaderState
import com.example.fitnesstracker.ui.theme.LeafCard as Card
import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import com.example.fitnesstracker.ui.components.LeafEditSheet
import com.example.fitnesstracker.ui.components.LeafSwitch
import com.example.fitnesstracker.ui.components.LeafButton
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.semantics.Role
import com.example.fitnesstracker.ui.components.LeafIcons
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.LanguageOptions
import com.example.fitnesstracker.ui.components.LanguageFlag
import com.example.fitnesstracker.ui.components.languageName
import com.example.fitnesstracker.ui.components.bouncyClick
import com.example.fitnesstracker.ui.theme.AppThemeMode
import com.example.fitnesstracker.utils.LanguagePreferences
import com.example.fitnesstracker.utils.WaterReminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onBack: () -> Unit,
    onResetData: () -> Unit
) {
    val context = LocalContext.current
    var waterReminders by remember { mutableStateOf(WaterReminder.isEnabled(context)) }
    var bgSteps by remember { mutableStateOf(com.example.fitnesstracker.utils.StepTrackingPreferences.isBackgroundEnabled(context)) }
    val stepPermission = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        bgSteps = granted
        com.example.fitnesstracker.utils.StepTrackingPreferences.setBackgroundEnabled(context, granted)
        if (granted) com.example.fitnesstracker.utils.StepCounterService.start(context)
    }
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        LeafEditSheet(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.settings_reset_confirm)) },
            content = { Text(stringResource(R.string.settings_reset_desc)) },
            confirmButton = {
                LeafButton(
                    onClick = { onResetData(); showResetDialog = false; onBack() },
                    shape   = RoundedCornerShape(50),
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.common_delete)) }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text(stringResource(R.string.common_cancel)) } }
        )
    }

    val headerState = rememberLeafHeaderState()
    LeafScreenScaffold(
        headerState = headerState,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            com.example.fitnesstracker.ui.components.CompactPageTopBar(
                stringResource(R.string.settings_title_app), stringResource(R.string.settings_header_hint), LeafIcons.Tune, onBack, headerState)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 12.dp, bottom = 48.dp)
        ) {


            item {
                SettingsGroupLabel(stringResource(R.string.settings_theme_section))
                ThemeModeSelector(
                    selected = themeMode,
                    onChange = onThemeModeChange
                )
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) item {
                var dynamic by remember { mutableStateOf(com.example.fitnesstracker.ui.theme.ThemePreferences.dynamicColor(context)) }
                SettingsSwitchItem(
                    icon = LeafIcons.Palette, iconColor = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.settings_dynamic_color),
                    subtitle = stringResource(R.string.settings_dynamic_color_hint),
                    checked = dynamic,
                    onToggle = { dynamic = it; com.example.fitnesstracker.ui.theme.ThemePreferences.setDynamicColor(context, it) }
                )
            }

            item {
                SettingsGroupLabel(stringResource(R.string.settings_language))
                LanguageSelector()
            }

            item {
                SettingsGroupLabel(stringResource(R.string.settings_notifications_section))
                SettingsSwitchItem(
                    icon = com.example.fitnesstracker.ui.components.LeafIcons.WaterDrop, iconColor = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.settings_water_reminder),
                    subtitle = stringResource(R.string.settings_water_reminder_sub),
                    checked = waterReminders,
                    groupIndex = 0, groupCount = 3,
                    onToggle = {
                        waterReminders = it
                        WaterReminder.setEnabled(context, it)
                    }
                )
                Spacer(Modifier.height(8.dp))
                var bedtimeOn by remember { mutableStateOf(com.example.fitnesstracker.utils.BedtimeReminder.isEnabled(context)) }
                SettingsSwitchItem(
                    icon = com.example.fitnesstracker.ui.components.LeafIcons.Bedtime, iconColor = MaterialTheme.colorScheme.tertiary,
                    title = stringResource(R.string.settings_bedtime),
                    subtitle = stringResource(R.string.settings_bedtime_sub,
                        com.example.fitnesstracker.utils.BedtimeReminder.getHour(context),
                        com.example.fitnesstracker.utils.BedtimeReminder.getMinute(context)),
                    checked = bedtimeOn,
                    groupIndex = 1, groupCount = 3,
                    onToggle = {
                        bedtimeOn = it
                        com.example.fitnesstracker.utils.BedtimeReminder.setEnabled(context, it)
                    }
                )
                Spacer(Modifier.height(8.dp))
                SettingsSwitchItem(
                    icon = com.example.fitnesstracker.ui.components.LeafIcons.DirectionsWalk, iconColor = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.settings_bg_steps),
                    subtitle = stringResource(R.string.settings_bg_steps_sub),
                    checked = bgSteps,
                    groupIndex = 2, groupCount = 3,
                    onToggle = {
                        if (it && !com.example.fitnesstracker.utils.StepCounter.hasActivityRecognitionPermission(context)) {
                            stepPermission.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
                        } else {
                            bgSteps = it
                            com.example.fitnesstracker.utils.StepTrackingPreferences.setBackgroundEnabled(context, it)
                            if (it) com.example.fitnesstracker.utils.StepCounterService.start(context)
                            else com.example.fitnesstracker.utils.StepCounterService.stop(context)
                        }
                    }
                )
            }

            item {
                SettingsGroupLabel(stringResource(R.string.settings_data_section))
                BackupSection()
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().bouncyClick { showResetDialog = true },
                    shape    = RoundedCornerShape(26.dp),
                    colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(LeafIcons.DeleteForever, null,
                                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.settings_reset), fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.settings_reset_sub),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(com.example.fitnesstracker.ui.components.LeafIcons.ChevronRight, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            item(key = "app_version") { AppVersionCard() }
        }
    }
}

@Composable
private fun ThemeModeSelector(selected: AppThemeMode, onChange: (AppThemeMode) -> Unit) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(Modifier.size(42.dp), Alignment.Center) {
                        Icon(LeafIcons.Palette, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.settings_theme_system), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).selectableGroup()) {
                ThemeModeChip(
                    text = stringResource(R.string.settings_theme_light),
                    icon = LeafIcons.WbSunny,
                    selected = selected == AppThemeMode.LIGHT,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onChange(AppThemeMode.LIGHT)
                    }
                )
                ThemeModeChip(
                    text = stringResource(R.string.settings_theme_dark),
                    icon = LeafIcons.DarkMode,
                    selected = selected == AppThemeMode.DARK,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onChange(AppThemeMode.DARK)
                    }
                )
                ThemeModeChip(
                    text = stringResource(R.string.settings_theme_system),
                    icon = com.example.fitnesstracker.ui.components.LeafIcons.PhoneAndroid,
                    selected = selected == AppThemeMode.SYSTEM,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onChange(AppThemeMode.SYSTEM)
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeModeChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val corner by androidx.compose.animation.core.animateDpAsState(if (selected) 24.dp else 14.dp,
        spring(.88f, 500f), label = "themeSelectionShape")
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "themeBg"
    )
    val fg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "themeFg"
    )
    Column(
        modifier = modifier
            .fillMaxHeight().clip(RoundedCornerShape(corner))
            .background(bg)
            .selectable(selected, role = Role.RadioButton,
                interactionSource = remember { MutableInteractionSource() }, indication = null,
                onClick = { if (!selected) onClick() })
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ThemeMiniPreview(icon = icon)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, color = fg, fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f, fill = false))
        }
    }
}

@Composable
private fun ThemeMiniPreview(icon: ImageVector) {
    val isDark = icon == LeafIcons.DarkMode
    val isAuto = icon == com.example.fitnesstracker.ui.components.LeafIcons.PhoneAndroid
    val bgLight = com.example.fitnesstracker.ui.theme.LeafLightColors.surface
    val bgDark = com.example.fitnesstracker.ui.theme.LeafDarkColors.surface
    val accLight = com.example.fitnesstracker.ui.theme.LeafLightColors.primary
    val accDark = com.example.fitnesstracker.ui.theme.LeafDarkColors.primary
    Box(
        Modifier.size(width = 52.dp, height = 34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDark) bgDark else bgLight)
    ) {
        if (isAuto) {
            Box(Modifier.fillMaxHeight().width(26.dp).align(Alignment.CenterEnd).background(bgDark))
        }
        Column(Modifier.padding(5.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.size(width = 20.dp, height = 5.dp).clip(RoundedCornerShape(3.dp))
                .background(if (isDark) accDark else accLight))
            Box(Modifier.size(width = 34.dp, height = 4.dp).clip(RoundedCornerShape(2.dp))
                .background((if (isDark) Color.White else Color.Black).copy(alpha = 0.18f)))
            Box(Modifier.size(width = 28.dp, height = 4.dp).clip(RoundedCornerShape(2.dp))
                .background((if (isDark) Color.White else Color.Black).copy(alpha = 0.12f)))
        }
    }
}

@Composable
private fun SettingsGroupLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp))
}

@Composable
private fun SettingsSwitchItem(icon: ImageVector, iconColor: Color, title: String, subtitle: String, checked: Boolean,
    groupIndex: Int = 0, groupCount: Int = 1, onToggle: (Boolean) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val shape = com.example.fitnesstracker.ui.components.groupedLeafShape(groupIndex, groupCount)
    Card(shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth().clip(shape)
            .toggleable(value = checked, role = Role.Switch, interactionSource = interaction,
                indication = null, onValueChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onToggle(it)
                })) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = iconColor.copy(alpha = 0.16f)
            ) {
                Box(Modifier.size(42.dp), Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            LeafSwitch(checked = checked,
                thumbContent = if (checked) { { Icon(com.example.fitnesstracker.ui.components.LeafIcons.Check,
                    null, Modifier.size(16.dp)) } } else null,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedIconColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedBorderColor = Color.Transparent),
                onCheckedChange = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelector() {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    var current by remember { mutableStateOf(LanguagePreferences.get(context)) }
    var expanded by remember { mutableStateOf(false) }
    Surface(onClick = { expanded = true }, shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LanguageFlag(current)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(languageName(current), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(com.example.fitnesstracker.ui.components.LeafIcons.ExpandMore, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (expanded) ModalBottomSheet(
        onDismissRequest = { expanded = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.headlineSmall)
            LanguageOptions(current) { language ->
                current = language
                expanded = false
                LanguagePreferences.set(context, language)
                activity?.recreate()
            }
        }
    }
}

@Composable
private fun BackupSection() {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }

    val okMsg = stringResource(R.string.backup_done)
    val errMsg = stringResource(R.string.backup_error)

    val exportJson = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { scope.launch {
            status = if (com.example.fitnesstracker.utils.BackupManager.exportJson(context, it)) okMsg else errMsg
        } }
    }
    val importJson = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { scope.launch {
            status = if (com.example.fitnesstracker.utils.BackupManager.importJson(context, it)) okMsg else errMsg
        } }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(Modifier.size(42.dp), Alignment.Center) {
                        Icon(LeafIcons.SaveAlt, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.backup_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(status ?: stringResource(R.string.backup_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (status != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BackupActionRow(stringResource(R.string.backup_export), stringResource(R.string.release_backup_export_hint),
                    LeafIcons.Upload, top = true, onClick = { exportJson.launch("leafit_snapshot.json") })
                BackupActionRow(stringResource(R.string.backup_import), stringResource(R.string.release_backup_import_hint),
                    LeafIcons.FileDownload, top = false, onClick = { importJson.launch("application/json") })
            }
        }
    }
}

@Composable
private fun BackupActionRow(title: String, subtitle: String, icon: ImageVector, top: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(onClick = onClick, color = colors.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = if (top) 22.dp else 10.dp, topEnd = if (top) 22.dp else 10.dp,
            bottomStart = if (top) 10.dp else 22.dp, bottomEnd = if (top) 10.dp else 22.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, Modifier.size(22.dp), tint = colors.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Icon(LeafIcons.ChevronRight, null, Modifier.size(20.dp), tint = colors.onSurfaceVariant)
        }
    }
}
