package com.example.fitnesstracker.ui.screens.settings

import com.example.fitnesstracker.ui.components.LeafFilledTonalIconButton as FilledTonalIconButton
import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

import com.example.fitnesstracker.ui.components.LeafScreenScaffold
import com.example.fitnesstracker.ui.components.rememberLeafHeaderState
import com.example.fitnesstracker.ui.theme.LeafCard as Card
import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import com.example.fitnesstracker.ui.components.LeafEditSheet
import com.example.fitnesstracker.ui.components.LeafTonalButton
import com.example.fitnesstracker.ui.components.LeafButton
import com.example.fitnesstracker.ui.components.LeafIcons
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.AppTextField
import com.example.fitnesstracker.ui.components.bmiCategoryString
import com.example.fitnesstracker.ui.components.StepperDecimalField
import java.io.File
import com.example.fitnesstracker.data.entities.UserProfile
import com.example.fitnesstracker.utils.HealthCalc
import com.example.fitnesstracker.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit = {}) {
    val profile      by vm.profile.collectAsState()
    val workoutCount by vm.workoutCount.collectAsState()
    val sleepCount   by vm.sleepCount.collectAsState()

    var showEditDialog    by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val context = LocalContext.current
    var cropUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { cropUri = it }
    }

    cropUri?.let { uri ->
        com.example.fitnesstracker.ui.components.ImageCropDialog(
            sourceUri = uri,
            onCancel = { cropUri = null },
            onCropped = { bmp ->
                val savedPath = saveBitmapToInternalStorage(context, bmp)
                if (savedPath != null) vm.updateAvatar(savedPath)
                cropUri = null
            }
        )
    }

    if (showEditDialog && profile != null) {
        val snapCtx = LocalContext.current
        EditProfileDialog(profile = profile!!, onSave = { updated ->
            profile?.let { p -> com.example.fitnesstracker.utils.MeasurementHistory.snapshot(snapCtx, p.chestCm, p.waistCm, p.hipsCm, p.bicepCm) }
            vm.saveProfile(updated); showEditDialog = false
        }, onDismiss = { showEditDialog = false })
    }
    val headerState = rememberLeafHeaderState()
    LeafScreenScaffold(
        headerState = headerState,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            com.example.fitnesstracker.ui.components.CompactPageTopBar(
                stringResource(R.string.nav_profile), stringResource(R.string.profile_header_hint), LeafIcons.Person, onBack, headerState)
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 12.dp, bottom = 120.dp)
        ) {

            item(key = "identity") {
                ProfileHero(profile, workoutCount, sleepCount,
                    onPickPhoto = { imagePickerLauncher.launch("image/*") },
                    onEdit = { showEditDialog = true })
            }

            item {
                GoalsCard()
            }

            item {
                MeasurementsCard(profile)
            }

            item {
                val matrix by vm.activityMatrix.collectAsState()
                com.example.fitnesstracker.ui.components.ActivityMatrix(
                    intensities = matrix,
                    title = stringResource(R.string.profile_activity_matrix)
                )
            }
        }
    }
}

@Composable
private fun ProfileHero(profile: UserProfile?, workoutCount: Int, sleepCount: Int, onPickPhoto: () -> Unit, onEdit: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(com.example.fitnesstracker.ui.components.LeafCorners.card),
        color = colors.surfaceContainer) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(onClick = onPickPhoto, shape = CircleShape, color = colors.primaryContainer, modifier = Modifier.size(104.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        val avatar = profile?.avatarUri
                        if (avatar != null) AsyncImage(model = if (avatar.startsWith("/")) File(avatar) else avatar,
                            contentDescription = stringResource(R.string.profile_photo_tap),
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Text(profile?.name?.take(1)?.uppercase() ?: "?", style = MaterialTheme.typography.displaySmall,
                            color = colors.onPrimaryContainer)
                    }
                }
                Surface(onClick = onPickPhoto, shape = CircleShape, color = colors.primary, contentColor = colors.onPrimary,
                    border = androidx.compose.foundation.BorderStroke(3.dp, colors.surfaceContainer), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(LeafIcons.AddAPhoto, stringResource(R.string.profile_photo_tap), Modifier.size(18.dp))
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(profile?.name ?: "—", style = MaterialTheme.typography.headlineSmall, maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                profile?.let { p ->
                    Text(stringResource(R.string.profile_summary, p.age, p.weightKg, p.heightCm),
                        style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            profile?.let { p ->
                val bmi = HealthCalc.bmi(p.weightKg, p.heightCm)
                Surface(shape = CircleShape, color = colors.secondaryContainer, contentColor = colors.onSecondaryContainer) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(LeafIcons.MonitorWeight, null, Modifier.size(18.dp))
                        Text(stringResource(R.string.profile_bmi, "%.1f".format(bmi), bmiCategoryString(HealthCalc.bmiCategory(bmi))),
                            style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ProfileStatTile(Modifier.weight(1f).fillMaxHeight(), "$workoutCount", stringResource(R.string.profile_workouts),
                    LeafIcons.Exercise, colors.primary, RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 6.dp, bottomEnd = 6.dp))
                ProfileStatTile(Modifier.weight(1f).fillMaxHeight(), "$sleepCount", stringResource(R.string.profile_sleep_records),
                    LeafIcons.Bedtime, colors.tertiary, RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 20.dp, bottomEnd = 20.dp))
            }
            LeafButton(onClick = onEdit, enabled = profile != null, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(50)) {
                Icon(LeafIcons.PersonEdit, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.settings_edit_profile))
            }
        }
    }
}

@Composable
private fun ProfileEditGroup(icon: ImageVector, title: String, tone: com.example.fitnesstracker.ui.components.LeafTone,
    content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            com.example.fitnesstracker.ui.components.LeafCardHeader(icon, title, tone = tone,
                iconSize = com.example.fitnesstracker.ui.components.LeafIconSize.Small)
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(profile: UserProfile, onSave: (UserProfile) -> Unit, onDismiss: () -> Unit) {
    var name   by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(profile.name) }
    var age    by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(profile.age.toString()) }
    var gender by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(profile.gender) }
    var height by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(trimNumber(profile.heightCm)) }
    var weight by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(trimNumber(profile.weightKg)) }
    var chest  by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(profile.chestCm?.let(::trimNumber) ?: "") }
    var waist  by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(profile.waistCm?.let(::trimNumber) ?: "") }
    var hips   by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(profile.hipsCm?.let(::trimNumber) ?: "") }
    var bicep  by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(profile.bicepCm?.let(::trimNumber) ?: "") }

    val ageInt   = age.toIntOrNull()
    val heightF  = height.toFloatOrNull()
    val weightF  = weight.toFloatOrNull()

    val nameOk   = name.trim().isNotEmpty()
    val ageOk    = ageInt != null && ageInt in 5..100
    val heightOk = heightF != null && heightF in 100f..220f
    val weightOk = weightF != null && weightF in 20f..645f
    val canSave  = nameOk && ageOk && heightOk && weightOk

    fun optionalCm(s: String): Float? {
        val v = s.toFloatOrNull() ?: return null
        return if (v in 1f..250f) v else null
    }
    fun decimal(s: String) = s.replace(',', '.').filter { c -> c.isDigit() || c == '.' }.take(5)

    LeafEditSheet(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_edit_profile)) },
        content = {
            ProfileEditGroup(LeafIcons.Person, stringResource(R.string.profile_basics), com.example.fitnesstracker.ui.components.LeafTone.Primary) {
                AppTextField(name, { name = it.take(40) }, stringResource(R.string.profile_name), Modifier.fillMaxWidth(),
                    isError = !nameOk)
                Text(stringResource(R.string.profile_gender), style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                com.example.fitnesstracker.ui.components.LeafGenderChoices(gender, { gender = it }, listOf(
                    "male" to stringResource(R.string.profile_gender_male),
                    "female" to stringResource(R.string.profile_gender_female),
                    "other" to stringResource(R.string.profile_gender_other)))
            }
            ProfileEditGroup(LeafIcons.MonitorWeight, stringResource(R.string.profile_body), com.example.fitnesstracker.ui.components.LeafTone.Secondary) {
                AppTextField(age, { age = it.filter(Char::isDigit).take(3) }, stringResource(R.string.profile_age),
                    Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number,
                    isError = !ageOk, supportingText = if (!ageOk) stringResource(R.string.profile_age_hint) else null)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(height, { height = decimal(it) }, stringResource(R.string.profile_height), Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal, isError = !heightOk,
                        supportingText = if (!heightOk) stringResource(R.string.profile_height_hint) else null)
                    AppTextField(weight, { weight = decimal(it) }, stringResource(R.string.profile_weight), Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal, isError = !weightOk,
                        supportingText = if (!weightOk) stringResource(R.string.profile_weight_hint) else null)
                }
                if (heightOk && weightOk) {
                    val bmi = HealthCalc.bmi(weightF!!, heightF!!)
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer) {
                        Text(stringResource(R.string.profile_bmi, "%.1f".format(bmi), bmiCategoryString(HealthCalc.bmiCategory(bmi))),
                            Modifier.padding(horizontal = 14.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            ProfileEditGroup(LeafIcons.Straighten, stringResource(R.string.profile_measurements), com.example.fitnesstracker.ui.components.LeafTone.Tertiary) {
                Text(stringResource(R.string.profile_measurements_optional), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                val fields = listOf(Triple(R.string.profile_chest, chest, { v: String -> chest = v }),
                    Triple(R.string.profile_waist, waist, { v: String -> waist = v }),
                    Triple(R.string.profile_hips, hips, { v: String -> hips = v }),
                    Triple(R.string.profile_bicep, bicep, { v: String -> bicep = v }))
                fields.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { (label, value, update) ->
                            AppTextField(value, { update(decimal(it)) }, stringResource(label), Modifier.weight(1f),
                                keyboardType = KeyboardType.Decimal)
                        }
                    }
                }
            }
        },
        confirmButton = {
            LeafButton(
                enabled = canSave,
                onClick = { onSave(profile.copy(
                    name = name.trim(), gender = gender,
                    age = ageInt ?: profile.age,
                    heightCm = heightF ?: profile.heightCm,
                    weightKg = weightF ?: profile.weightKg,
                    chestCm  = optionalCm(chest),
                    waistCm  = optionalCm(waist),
                    hipsCm   = optionalCm(hips),
                    bicepCm  = optionalCm(bicep)
                )) },
                shape = RoundedCornerShape(50)
            ) {
                Icon(LeafIcons.Check, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    )
}

private fun trimNumber(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString() else "%.1f".format(java.util.Locale.US, value)

private fun copyAvatarToInternalStorage(context: Context, uri: Uri): String? {
    return try {

        context.filesDir.listFiles { f -> f.name.startsWith("avatar_") }?.forEach { it.delete() }
        val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
        val input = context.contentResolver.openInputStream(uri) ?: return null
        input.use { inp -> file.outputStream().use { out -> inp.copyTo(out) } }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}

private fun saveBitmapToInternalStorage(context: Context, bitmap: android.graphics.Bitmap): String? {
    return try {
        context.filesDir.listFiles { f -> f.name.startsWith("avatar_") }?.forEach { it.delete() }
        val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun ProfileStatTile(modifier: Modifier, value: String, label: String, icon: ImageVector, color: Color,
    shape: androidx.compose.ui.graphics.Shape) {
    Surface(modifier = modifier, shape = shape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null, Modifier.size(22.dp), tint = color)
            Column(Modifier.weight(1f)) {
                com.example.fitnesstracker.ui.components.MetricText(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
    }
}

private fun genderIcon(g: String): ImageVector = when (g) {
    "male"   -> LeafIcons.Male
    "female" -> LeafIcons.Female
    else      -> LeafIcons.QuestionMark
}

@Composable
private fun MeasurementsCard(profile: UserProfile?) {
    if (profile == null) return
    val items = listOf(
        Triple("chest", stringResource(R.string.profile_chest), profile.chestCm),
        Triple("waist", stringResource(R.string.profile_waist), profile.waistCm),
        Triple("hips", stringResource(R.string.profile_hips),  profile.hipsCm),
        Triple("bicep", stringResource(R.string.profile_bicep), profile.bicepCm),
    )
    val anyValue = items.any { it.third != null }
    if (!anyValue) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            com.example.fitnesstracker.ui.components.LeafCardHeader(LeafIcons.Straighten, stringResource(R.string.profile_measurements),
                tone = com.example.fitnesstracker.ui.components.LeafTone.Tertiary)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items.take(2).forEach { (key, label, value) ->
                    MeasurementTile(Modifier.weight(1f), label, value, key)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items.drop(2).forEach { (key, label, value) ->
                    MeasurementTile(Modifier.weight(1f), label, value, key)
                }
            }
        }
    }
}

@Composable
private fun MeasurementTile(modifier: Modifier = Modifier, label: String, valueCm: Float?, prefKey: String = "") {
    val ctx = LocalContext.current
    val prev = remember(prefKey) { if (prefKey.isNotEmpty()) com.example.fitnesstracker.utils.MeasurementHistory.previous(ctx, prefKey) else null }
    val delta = if (valueCm != null && prev != null && prev != valueCm) valueCm - prev else null
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                valueCm?.let { "${it.toInt()} ${androidx.compose.ui.res.stringResource(com.example.fitnesstracker.R.string.unit_cm)}" } ?: "—",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = com.example.fitnesstracker.ui.theme.NumericFontFamily,
                color = if (valueCm != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
            )
            if (delta != null) {
                val down = delta < 0f
                Text(
                    (if (down) "−" else "+") + "%.0f".format(kotlin.math.abs(delta)) + " " + stringResource(R.string.unit_cm),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (down) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GoalsCard() {
    val context = LocalContext.current
    var stepsGoal by remember { mutableStateOf(com.example.fitnesstracker.utils.GoalPreferences.getStepsGoal(context)) }
    var waterGoal by remember { mutableStateOf(com.example.fitnesstracker.utils.GoalPreferences.getWaterGoal(context)) }
    var sleepGoal by remember { mutableStateOf(com.example.fitnesstracker.utils.GoalPreferences.getSleepGoalMinutes(context)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            com.example.fitnesstracker.ui.components.LeafCardHeader(LeafIcons.SportsScore, stringResource(R.string.profile_goals),
                subtitle = stringResource(R.string.goals_personal_hint))
            Spacer(Modifier.height(10.dp))

            GoalStepperRow(
                icon = LeafIcons.Steps, index = 0,
                label = stringResource(R.string.profile_goal_steps),
                value = "$stepsGoal",
                color = MaterialTheme.colorScheme.secondary,
                onMinus = { if (stepsGoal > 1000) { stepsGoal -= 1000; com.example.fitnesstracker.utils.GoalPreferences.setStepsGoal(context, stepsGoal); com.example.fitnesstracker.utils.Haptics.perform(context, com.example.fitnesstracker.utils.HapticLevel.LIGHT) } },
                onPlus = { if (stepsGoal < 50000) { stepsGoal += 1000; com.example.fitnesstracker.utils.GoalPreferences.setStepsGoal(context, stepsGoal); com.example.fitnesstracker.utils.Haptics.perform(context, com.example.fitnesstracker.utils.HapticLevel.LIGHT) } }
            )
            GoalStepperRow(
                icon = LeafIcons.WaterFull, index = 1,
                label = stringResource(R.string.profile_goal_water),
                value = "$waterGoal ${stringResource(R.string.unit_ml)}",
                color = MaterialTheme.colorScheme.primary,
                onMinus = { if (waterGoal > 500) { waterGoal -= 250; com.example.fitnesstracker.utils.GoalPreferences.setWaterGoal(context, waterGoal); com.example.fitnesstracker.utils.Haptics.perform(context, com.example.fitnesstracker.utils.HapticLevel.LIGHT) } },
                onPlus = { if (waterGoal < 6000) { waterGoal += 250; com.example.fitnesstracker.utils.GoalPreferences.setWaterGoal(context, waterGoal); com.example.fitnesstracker.utils.Haptics.perform(context, com.example.fitnesstracker.utils.HapticLevel.LIGHT) } }
            )
            GoalStepperRow(
                icon = LeafIcons.Bedtime, index = 2,
                label = stringResource(R.string.profile_goal_sleep),
                value = "${sleepGoal / 60}${stringResource(R.string.unit_h)} ${sleepGoal % 60}${stringResource(R.string.unit_min_short)}",
                color = MaterialTheme.colorScheme.tertiary,
                onMinus = { if (sleepGoal > 240) { sleepGoal -= 30; com.example.fitnesstracker.utils.GoalPreferences.setSleepGoalMinutes(context, sleepGoal); com.example.fitnesstracker.utils.Haptics.perform(context, com.example.fitnesstracker.utils.HapticLevel.LIGHT) } },
                onPlus = { if (sleepGoal < 720) { sleepGoal += 30; com.example.fitnesstracker.utils.GoalPreferences.setSleepGoalMinutes(context, sleepGoal); com.example.fitnesstracker.utils.Haptics.perform(context, com.example.fitnesstracker.utils.HapticLevel.LIGHT) } }
            )
        }
    }
}

@Composable
private fun GoalStepperRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    index: Int = 0,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Surface(shape = com.example.fitnesstracker.ui.components.groupedLeafShape(index, 3),
        color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                com.example.fitnesstracker.ui.components.MetricText(value,
                    style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            FilledTonalIconButton(onClick = onMinus, modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                Icon(LeafIcons.Remove, stringResource(R.string.goal_decrease, label), tint = MaterialTheme.colorScheme.onSurface)
            }
            FilledTonalIconButton(onClick = onPlus, modifier = Modifier.size(44.dp)) {
                Icon(LeafIcons.Add, stringResource(R.string.goal_increase, label), tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}
