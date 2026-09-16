package com.example.fitnesstracker.ui.screens.settings

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
    val achievements by vm.achievements.collectAsState()
    val workoutCount by vm.workoutCount.collectAsState()
    val sleepCount   by vm.sleepCount.collectAsState()

    var showEditDialog    by remember { mutableStateOf(false) }
    var showAchievements  by remember { mutableStateOf(false) }

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
    if (showAchievements) {
        com.example.fitnesstracker.ui.components.AchievementsSheet(achievements, workoutCount, sleepCount,
            onDismiss = { showAchievements = false })
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

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.profile_header_hint),
                            style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))

                        val matrix by vm.activityMatrix.collectAsState()
                        val dayProgress = matrix.lastOrNull() ?: 0f
                        val animProgress by animateFloatAsState(
                            dayProgress.coerceIn(0f, 1f),
                            tween(1000, easing = EaseOutCubic),
                            label = "dayProgress"
                        )
                        val ringColor = MaterialTheme.colorScheme.primary
                        val ringTrack = MaterialTheme.colorScheme.surfaceVariant
                        Box(
                            modifier = Modifier.size(102.dp).drawBehind {
                                val stroke = 4.dp.toPx()
                                val inset = stroke / 2f
                                drawArc(
                                    color = ringTrack.copy(alpha = 0.4f),
                                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                                    topLeft = Offset(inset, inset),
                                    size = Size(size.width - stroke, size.height - stroke),
                                    style = Stroke(stroke)
                                )
                                drawArc(
                                    color = ringColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f * animProgress,
                                    useCenter = false,
                                    topLeft = Offset(inset, inset),
                                    size = Size(size.width - stroke, size.height - stroke),
                                    style = Stroke(stroke, cap = StrokeCap.Round)
                                )
                            },
                            contentAlignment = Alignment.Center
                        ) {
                        Box(
                            modifier = Modifier.size(93.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                        Box(
                            modifier = Modifier.size(86.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profile?.avatarUri != null) {
                                val uri = profile!!.avatarUri!!
                                AsyncImage(model = if (uri.startsWith("/")) File(uri) else uri, contentDescription = null,
                                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Text(profile?.name?.take(1)?.uppercase() ?: "?",
                                    style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.profile_photo_tap), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(profile?.name ?: "—", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                        profile?.let { p ->
                            Text(stringResource(R.string.profile_summary, p.age, p.weightKg, p.heightCm),
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val bmi = HealthCalc.bmi(p.weightKg, p.heightCm)
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.clip(RoundedCornerShape(50.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                Text(stringResource(R.string.profile_bmi, "%.1f".format(bmi), bmiCategoryString(HealthCalc.bmiCategory(bmi))),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }

            item {
                val unlockedCount = achievements.count { it.unlockedAt != null }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileStatTile(Modifier.weight(1f), "$workoutCount", stringResource(R.string.profile_workouts), MaterialTheme.colorScheme.primary)
                    ProfileStatTile(Modifier.weight(1f), "$sleepCount", stringResource(R.string.profile_sleep_records), MaterialTheme.colorScheme.tertiary)
                    ProfileStatTile(Modifier.weight(1f), "$unlockedCount", stringResource(R.string.profile_achievements), MaterialTheme.colorScheme.secondary)
                }
            }

            item {
                MeasurementsCard(profile)
            }

            item {
                GoalsCard()
            }

            item {
                LeafTonalButton(onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(50)) {
                    Icon(com.example.fitnesstracker.ui.components.LeafIcons.Edit, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_edit_profile))
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(profile: UserProfile, onSave: (UserProfile) -> Unit, onDismiss: () -> Unit) {
    var name   by remember { mutableStateOf(profile.name) }
    var age    by remember { mutableStateOf(profile.age.toString()) }
    var gender by remember { mutableStateOf(profile.gender) }
    var height by remember { mutableStateOf(profile.heightCm.toString()) }
    var weight by remember { mutableStateOf(profile.weightKg.toString()) }
    var chest  by remember { mutableStateOf(profile.chestCm?.let { "%.0f".format(it) } ?: "") }
    var waist  by remember { mutableStateOf(profile.waistCm?.let { "%.0f".format(it) } ?: "") }
    var hips   by remember { mutableStateOf(profile.hipsCm?.let { "%.0f".format(it) } ?: "") }
    var bicep  by remember { mutableStateOf(profile.bicepCm?.let { "%.0f".format(it) } ?: "") }

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

    LeafEditSheet(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_edit_profile)) },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(value = name, onValueChange = { name = it.take(40) },
                    label = stringResource(R.string.profile_name),
                    modifier = Modifier.fillMaxWidth())
                com.example.fitnesstracker.ui.components.LeafGenderChoices(gender, { gender = it }, listOf(
                    "male" to stringResource(R.string.profile_gender_male),
                    "female" to stringResource(R.string.profile_gender_female),
                    "other" to stringResource(R.string.profile_gender_other)))
                StepperDecimalField(
                    value = age,
                    onChange = { age = it.filter { c -> c.isDigit() }.take(3) },
                    label = stringResource(R.string.profile_age), step = 1f, max = 100f,
                    isError = age.isNotEmpty() && !ageOk,
                    supportingText = stringResource(R.string.profile_age_hint),
                    modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StepperDecimalField(
                        value = height,
                        onChange = { height = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                        label = stringResource(R.string.profile_height), step = 1f, max = 220f,
                        isError = height.isNotEmpty() && !heightOk,
                        supportingText = stringResource(R.string.profile_height_hint),
                        modifier = Modifier.weight(1f))
                    StepperDecimalField(
                        value = weight,
                        onChange = { weight = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                        label = stringResource(R.string.profile_weight), step = 0.5f, max = 645f,
                        isError = weight.isNotEmpty() && !weightOk,
                        supportingText = stringResource(R.string.profile_weight_hint),
                        modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(LeafIcons.Straighten, null,
                                tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                            Text(stringResource(R.string.profile_measurements),
                                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.weight(1f))
                            Text(stringResource(R.string.profile_measurements_optional),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppTextField(value = chest, onValueChange = { chest = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                                label = stringResource(R.string.profile_chest), modifier = Modifier.weight(1f),
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                            AppTextField(value = waist, onValueChange = { waist = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                                label = stringResource(R.string.profile_waist), modifier = Modifier.weight(1f),
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppTextField(value = hips, onValueChange = { hips = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                                label = stringResource(R.string.profile_hips), modifier = Modifier.weight(1f),
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                            AppTextField(value = bicep, onValueChange = { bicep = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                                label = stringResource(R.string.profile_bicep), modifier = Modifier.weight(1f),
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
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
            ) { Text(stringResource(R.string.common_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    )
}

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
private fun ProfileStatTile(modifier: Modifier, value: String, label: String, color: Color) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            com.example.fitnesstracker.ui.components.MetricText(value, style = MaterialTheme.typography.titleLarge, weight = FontWeight.SemiBold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Box(Modifier.size(40.dp), Alignment.Center) {
                        Icon(LeafIcons.Straighten, null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(20.dp))
                    }
                }
                Text(stringResource(R.string.profile_measurements),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
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
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                valueCm?.let { "${it.toInt()} ${androidx.compose.ui.res.stringResource(com.example.fitnesstracker.R.string.unit_cm)}" } ?: "—",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (valueCm != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
            )
            if (delta != null) {
                val down = delta < 0f
                Text(
                    (if (down) "−" else "+") + "%.0f".format(kotlin.math.abs(delta)) + " " + stringResource(R.string.unit_cm),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
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
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(Modifier.size(40.dp), Alignment.Center) {
                        Icon(LeafIcons.Flag, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                    }
                }
                Text(stringResource(R.string.profile_goals), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }

            Text(stringResource(R.string.goals_personal_hint), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            GoalStepperRow(
                icon = com.example.fitnesstracker.ui.components.LeafIcons.DirectionsWalk,
                label = stringResource(R.string.profile_goal_steps),
                value = "$stepsGoal",
                color = MaterialTheme.colorScheme.secondary,
                onMinus = { if (stepsGoal > 1000) { stepsGoal -= 1000; com.example.fitnesstracker.utils.GoalPreferences.setStepsGoal(context, stepsGoal); com.example.fitnesstracker.utils.Haptics.perform(context, com.example.fitnesstracker.utils.HapticLevel.LIGHT) } },
                onPlus = { if (stepsGoal < 50000) { stepsGoal += 1000; com.example.fitnesstracker.utils.GoalPreferences.setStepsGoal(context, stepsGoal); com.example.fitnesstracker.utils.Haptics.perform(context, com.example.fitnesstracker.utils.HapticLevel.LIGHT) } }
            )
            GoalStepperRow(
                icon = com.example.fitnesstracker.ui.components.LeafIcons.WaterDrop,
                label = stringResource(R.string.profile_goal_water),
                value = "$waterGoal ${stringResource(R.string.unit_ml)}",
                color = MaterialTheme.colorScheme.primary,
                onMinus = { if (waterGoal > 500) { waterGoal -= 250; com.example.fitnesstracker.utils.GoalPreferences.setWaterGoal(context, waterGoal); com.example.fitnesstracker.utils.Haptics.perform(context, com.example.fitnesstracker.utils.HapticLevel.LIGHT) } },
                onPlus = { if (waterGoal < 6000) { waterGoal += 250; com.example.fitnesstracker.utils.GoalPreferences.setWaterGoal(context, waterGoal); com.example.fitnesstracker.utils.Haptics.perform(context, com.example.fitnesstracker.utils.HapticLevel.LIGHT) } }
            )
            GoalStepperRow(
                icon = com.example.fitnesstracker.ui.components.LeafIcons.Bedtime,
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
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest) {
                    Box(Modifier.size(38.dp), Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(22.dp)) }
                }
                Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                com.example.fitnesstracker.ui.components.MetricText(value,
                    style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface,
                    weight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                FilledTonalIconButton(onClick = onMinus, modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                    Icon(LeafIcons.Remove, stringResource(R.string.goal_decrease, label), tint = MaterialTheme.colorScheme.onSurface)
                }
                FilledTonalIconButton(onClick = onPlus, modifier = Modifier.size(48.dp)) {
                    Icon(LeafIcons.Add, stringResource(R.string.goal_increase, label), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}
