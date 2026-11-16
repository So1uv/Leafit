package com.example.fitnesstracker.ui.screens.appsettings

import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

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
import com.example.fitnesstracker.ui.components.LeafIconBadge
import com.example.fitnesstracker.ui.components.LeafIconSize
import com.example.fitnesstracker.ui.components.LeafTone
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    var quietHours by remember { mutableStateOf(WaterReminder.quietHours(context)) }
    var bgSteps by remember { mutableStateOf(com.example.fitnesstracker.utils.StepTrackingPreferences.isBackgroundEnabled(context)) }
    val stepPermission = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        bgSteps = granted
        com.example.fitnesstracker.utils.StepTrackingPreferences.setBackgroundEnabled(context, granted)
        if (granted) com.example.fitnesstracker.utils.StepCounterService.start(context)
    }
    var showResetDialog by remember { mutableStateOf(false) }
    var search by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val hasDynamic = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
    val sectionKeys = listOf("search", "appearance_header", "theme") + (if (hasDynamic) listOf("dynamic") else emptyList()) +
        listOf("language", "notifications_header", "water", "quiet", "bedtime",
            "activity_header", "steps", "background", "training", "challenges",
            "interaction_header", "accessibility", "widgets", "data_header", "data", "reset", "about_header", "app_version")
    val targets = listOf(
        "theme" to stringResource(R.string.settings_theme),
        "language" to stringResource(R.string.settings_language),
        "water" to stringResource(R.string.settings_water_reminder),
        "quiet" to stringResource(R.string.settings_water_quiet),
        "bedtime" to stringResource(R.string.settings_bedtime),
        "steps" to stringResource(R.string.settings_bg_steps),
        "data" to stringResource(R.string.settings_data_section),
        "data" to stringResource(R.string.backup_title),
        "reset" to stringResource(R.string.settings_reset),
        "training" to stringResource(R.string.timer_settings_title),
        "accessibility" to stringResource(R.string.haptic_title),
        "accessibility" to stringResource(R.string.haptic_strength),
        "widgets" to stringResource(R.string.widget_settings_title),
        "challenges" to stringResource(R.string.challenges_title),
        "background" to stringResource(R.string.settings_background_status),
        "app_version" to stringResource(R.string.release_build, 18L)) +
        if (hasDynamic) listOf("dynamic" to stringResource(R.string.settings_dynamic_color)) else emptyList()

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
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 12.dp, bottom = 48.dp)
        ) {


            item(key = "search") {
                com.example.fitnesstracker.ui.components.AppSearchBar(search, { search = it },
                    stringResource(R.string.settings_search), Modifier.fillMaxWidth())
                val query = search.trim()
                val hits = if (query.isBlank()) emptyList()
                    else targets.filter { it.second.contains(query, ignoreCase = true) }.distinctBy { it.first }
                if (query.isNotBlank() && hits.isEmpty())
                    Text(stringResource(R.string.settings_no_results), Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (hits.isNotEmpty()) Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    hits.forEachIndexed { index, (key, title) ->
                        Surface(onClick = {
                            search = ""
                            scope.launch { listState.animateScrollToItem(sectionKeys.indexOf(key).coerceAtLeast(0)) }
                        }, shape = com.example.fitnesstracker.ui.components.groupedLeafShape(index, hits.size),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(LeafIcons.Search, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                                Icon(LeafIcons.ChevronRight, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item(key = "appearance_header") { SettingsGroupLabel(stringResource(R.string.settings_theme_section)) }
            item(key = "theme") {
                ThemeModeSelector(selected = themeMode, onChange = onThemeModeChange)
            }
            if (hasDynamic) item(key = "dynamic") {
                var dynamic by remember { mutableStateOf(com.example.fitnesstracker.ui.theme.ThemePreferences.dynamicColor(context)) }
                SettingsSwitchItem(
                    icon = LeafIcons.Palette, tone = LeafTone.Primary,
                    title = stringResource(R.string.settings_dynamic_color),
                    subtitle = stringResource(R.string.settings_dynamic_color_hint),
                    checked = dynamic,
                    onToggle = { dynamic = it; com.example.fitnesstracker.ui.theme.ThemePreferences.setDynamicColor(context, it) }
                )
            }
            item(key = "language") { LanguageSelector() }

            item(key = "notifications_header") { SettingsGroupLabel(stringResource(R.string.settings_notifications_section)) }
            item(key = "water") {
                SettingsSwitchItem(
                    icon = LeafIcons.WaterFull, tone = LeafTone.Primary,
                    title = stringResource(R.string.settings_water_reminder),
                    subtitle = stringResource(R.string.settings_water_reminder_sub),
                    checked = waterReminders, groupIndex = 0, groupCount = 3,
                    onToggle = { waterReminders = it; WaterReminder.setEnabled(context, it) }
                )
            }
            item(key = "quiet") {
                SettingsSwitchItem(
                    icon = LeafIcons.MoonStars, tone = LeafTone.Tertiary,
                    title = stringResource(R.string.settings_water_quiet),
                    subtitle = stringResource(R.string.settings_water_quiet_sub),
                    checked = quietHours, groupIndex = 1, groupCount = 3,
                    onToggle = { quietHours = it; WaterReminder.setQuietHours(context, it) }
                )
            }
            item(key = "bedtime") {
                var bedtimeOn by remember { mutableStateOf(com.example.fitnesstracker.utils.BedtimeReminder.isEnabled(context)) }
                SettingsSwitchItem(
                    icon = LeafIcons.Bedtime, tone = LeafTone.Tertiary,
                    title = stringResource(R.string.settings_bedtime),
                    subtitle = stringResource(R.string.settings_bedtime_sub,
                        com.example.fitnesstracker.utils.BedtimeReminder.getHour(context),
                        com.example.fitnesstracker.utils.BedtimeReminder.getMinute(context)),
                    checked = bedtimeOn, groupIndex = 2, groupCount = 3,
                    onToggle = { bedtimeOn = it; com.example.fitnesstracker.utils.BedtimeReminder.setEnabled(context, it) }
                )
            }

            item(key = "activity_header") { SettingsGroupLabel(stringResource(R.string.b7_settings_activity)) }
            item(key = "steps") {
                SettingsSwitchItem(
                    icon = LeafIcons.Steps, tone = LeafTone.Secondary,
                    title = stringResource(R.string.settings_bg_steps),
                    subtitle = stringResource(R.string.settings_bg_steps_sub),
                    checked = bgSteps, groupIndex = 0, groupCount = 2,
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
            item(key = "background") {
                val restricted = android.os.Build.VERSION.SDK_INT >= 28 &&
                    context.getSystemService(android.app.ActivityManager::class.java).isBackgroundRestricted
                SettingsNavItem(
                    icon = LeafIcons.Shield, tone = if (restricted) LeafTone.Error else LeafTone.Secondary,
                    title = stringResource(R.string.settings_background_status),
                    subtitle = stringResource(if (restricted) R.string.settings_background_restricted else R.string.settings_background_ready),
                    groupIndex = 1, groupCount = 2,
                    onClick = { runCatching { context.startActivity(android.content.Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.parse("package:${context.packageName}"))) } })
            }
            item(key = "training") { ExperienceSettings("timers") }
            item(key = "challenges") { com.example.fitnesstracker.ui.screens.home.ChallengeVisibilitySettings() }

            item(key = "interaction_header") { SettingsGroupLabel(stringResource(R.string.b7_settings_interaction)) }
            item(key = "accessibility") { ExperienceSettings("haptics") }
            item(key = "widgets") { ExperienceSettings("widgets") }

            item(key = "data_header") { SettingsGroupLabel(stringResource(R.string.settings_data_section)) }
            item(key = "data") { BackupSection() }
            item(key = "reset") {
                SettingsNavItem(
                    icon = LeafIcons.DeleteForever, tone = LeafTone.Error,
                    title = stringResource(R.string.settings_reset),
                    subtitle = stringResource(R.string.settings_reset_sub),
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { showResetDialog = true })
            }

            item(key = "about_header") { SettingsGroupLabel(stringResource(R.string.b7_settings_about)) }
            item(key = "app_version") { AppVersionCard() }
        }
    }
}

@Composable
private fun ThemeModeSelector(selected: AppThemeMode, onChange: (AppThemeMode) -> Unit) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            com.example.fitnesstracker.ui.components.LeafCardHeader(LeafIcons.Contrast, stringResource(R.string.settings_theme),
                subtitle = stringResource(when (selected) {
                    AppThemeMode.LIGHT -> R.string.settings_theme_light
                    AppThemeMode.DARK -> R.string.settings_theme_dark
                    AppThemeMode.SYSTEM -> R.string.settings_theme_system
                }))
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
            com.example.fitnesstracker.ui.components.LeafFitText(text, Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.labelMedium, color = fg)
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
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 20.dp, bottom = 4.dp))
}

@Composable
private fun SettingsNavItem(icon: ImageVector, tone: LeafTone, title: String, subtitle: String,
    groupIndex: Int = 0, groupCount: Int = 1, titleColor: Color = Color.Unspecified, onClick: () -> Unit) {
    val shape = com.example.fitnesstracker.ui.components.groupedLeafShape(groupIndex, groupCount)
    Surface(onClick = onClick, shape = shape, color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            LeafIconBadge(icon, size = LeafIconSize.Medium, tone = tone)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium,
                    color = if (titleColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else titleColor)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Icon(LeafIcons.ChevronRight, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsSwitchItem(icon: ImageVector, tone: LeafTone, title: String, subtitle: String, checked: Boolean,
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
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            LeafIconBadge(icon, size = LeafIconSize.Medium, tone = tone)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            LeafSwitch(checked = checked,
                thumbContent = if (checked) { { Icon(com.example.fitnesstracker.ui.components.LeafIcons.Check,
                    null, Modifier.size(16.dp)) } } else null,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedIconColor = MaterialTheme.colorScheme.primary,
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
    Surface(onClick = { expanded = true }, shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LanguageFlag(current)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
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
            com.example.fitnesstracker.ui.components.LeafCardHeader(LeafIcons.Database, stringResource(R.string.backup_title),
                subtitle = status ?: stringResource(R.string.backup_subtitle))
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
            LeafIconBadge(icon, size = LeafIconSize.Small, tone = LeafTone.Neutral)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Icon(LeafIcons.ChevronRight, null, Modifier.size(20.dp), tint = colors.onSurfaceVariant)
        }
    }
}
