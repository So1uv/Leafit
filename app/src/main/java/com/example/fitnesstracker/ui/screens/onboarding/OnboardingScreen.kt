package com.example.fitnesstracker.ui.screens.onboarding

import com.example.fitnesstracker.ui.theme.LeafCard as Card
import com.example.fitnesstracker.ui.components.LeafButton
import com.example.fitnesstracker.ui.components.LeafIcons
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.UserProfile
import com.example.fitnesstracker.ui.components.AppTextField
import com.example.fitnesstracker.ui.components.StepperDecimalField
import com.example.fitnesstracker.ui.components.bmiCategoryString
import com.example.fitnesstracker.utils.AppLanguage
import com.example.fitnesstracker.utils.HealthCalc
import com.example.fitnesstracker.utils.LanguagePreferences
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: (UserProfile) -> Unit,
    onImportSnapshot: (android.net.Uri, (Boolean) -> Unit) -> Unit = { _, _ -> }
) {
    var step by rememberSaveable { mutableIntStateOf(0) }

    var name   by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("male") }
    var avatarPath by rememberSaveable { mutableStateOf<String?>(null) }

    var age    by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }

    var chest  by rememberSaveable { mutableStateOf("") }
    var waist  by rememberSaveable { mutableStateOf("") }
    var hips   by rememberSaveable { mutableStateOf("") }
    var bicep  by rememberSaveable { mutableStateOf("") }

    val haptic  = LocalHapticFeedback.current
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    val ageI    = age.toIntOrNull() ?: 0
    val heightF = height.toFloatOrNull() ?: 0f
    val weightF = weight.toFloatOrNull() ?: 0f

    val step1Ready = name.trim().isNotEmpty()
    val step2Ready = ageI in 5..100 && heightF in 100f..220f && weightF in 20f..645f

    val steps = listOf(
        stringResource(R.string.onboarding_step1),
        stringResource(R.string.onboarding_step2),
        stringResource(R.string.onboarding_step3)
    )

    val density = androidx.compose.ui.platform.LocalDensity.current
    var footerHeightPx by remember { mutableIntStateOf(0) }
    val footerHeight = with(density) { if (footerHeightPx > 0) footerHeightPx.toDp() else 148.dp }
    var importing by remember { mutableStateOf(false) }
    var importError by remember { mutableStateOf(false) }
    val snapshotPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            importing = true; importError = false
            onImportSnapshot(it) { ok -> importing = false; importError = !ok }
        }
    }
    val scrollState = rememberScrollState()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    LaunchedEffect(step) { focusManager.clearFocus(); scrollState.scrollTo(0) }
    val palette = MaterialTheme.colorScheme
    val canAdvance = !importing && when (step) { 0 -> step1Ready; else -> step1Ready && step2Ready }
    Box(Modifier.fillMaxSize().background(palette.surface).statusBarsPadding()
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)).imePadding()) {
        Column(Modifier.fillMaxSize().verticalScroll(scrollState)
            .padding(horizontal = 20.dp).padding(top = 16.dp, bottom = footerHeight + 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            com.example.fitnesstracker.ui.components.OnboardingProgress(step, steps) { previous ->
                if (!importing && previous < step) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    step = previous
                }
            }
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(spring(.93f, 420f)) { direction * it / 5 } + fadeIn(tween(180)))
                        .togetherWith(slideOutHorizontally(tween(140)) { -direction * it / 7 } + fadeOut(tween(110)))
                },
                label = "onboardingPage"
            ) { currentStep ->
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    when (currentStep) {
                        0 -> Step1Content(
                            name = name, onNameChange = { name = it },
                            gender = gender, onGenderChange = { gender = it },
                            avatarPath = avatarPath, onAvatarChange = { avatarPath = it },
                            currentLang = LanguagePreferences.get(context),
                            onLangChange = { lang ->
                                LanguagePreferences.set(context, lang)
                                activity?.recreate()
                            }
                        )
                        1 -> Step2Content(
                            age = age, onAgeChange = { age = it.filter { c -> c.isDigit() }.take(3) },
                            height = height, onHeightChange = { height = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                            weight = weight, onWeightChange = { weight = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                            bmi = if (heightF in 100f..220f && weightF in 20f..645f) HealthCalc.bmi(weightF, heightF) else 0f,
                            calories = if (ageI in 5..100 && heightF in 100f..220f && weightF in 20f..645f)
                                HealthCalc.dailyCalories(weightF, heightF, ageI, gender).roundToInt() else 0
                        )
                        2 -> Step3Content(
                            chest = chest, onChestChange = { chest = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                            waist = waist, onWaistChange = { waist = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                            hips  = hips,  onHipsChange  = { hips  = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                            bicep = bicep, onBicepChange = { bicep = it.filter { c -> c.isDigit() || c == '.' }.take(5) }
                        )
                    }
                }
            }
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            .onSizeChanged { footerHeightPx = it.height }
            .navigationBarsPadding().padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 12.dp)
            .background(palette.surfaceContainerLow, RoundedCornerShape(32.dp)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (importError) Text(stringResource(R.string.onboarding_import_error),
                style = MaterialTheme.typography.bodySmall, color = palette.error)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (step > 0) FilledTonalIconButton(enabled = !importing, onClick = { step-- },
                    modifier = Modifier.size(56.dp)) {
                    Icon(com.example.fitnesstracker.ui.components.LeafIcons.ArrowBack, stringResource(R.string.common_back))
                }
                LeafButton(
                    enabled = canAdvance,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (step < 2) step++ else onFinish(UserProfile(
                            name = name.trim(), gender = gender, age = ageI, avatarUri = avatarPath,
                            heightCm = heightF, weightKg = weightF,
                            chestCm = chest.toFloatOrNull()?.takeIf { it in 1f..250f },
                            waistCm = waist.toFloatOrNull()?.takeIf { it in 1f..250f },
                            hipsCm = hips.toFloatOrNull()?.takeIf { it in 1f..250f },
                            bicepCm = bicep.toFloatOrNull()?.takeIf { it in 1f..250f }))
                    }, modifier = Modifier.weight(1f).heightIn(min = 56.dp), shape = RoundedCornerShape(50)
                ) {
                    Text(stringResource(if (step < 2) R.string.common_continue_next else R.string.common_start),
                        style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(10.dp))
                    Icon(if (step < 2) com.example.fitnesstracker.ui.components.LeafIcons.ArrowForward
                        else com.example.fitnesstracker.ui.components.LeafIcons.Check, null, Modifier.size(20.dp))
                }
            }
            if (step == 0) TextButton(enabled = !importing,
                onClick = { snapshotPicker.launch("application/json") }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                if (importing) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else {
                    Icon(LeafIcons.Restore, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.onboarding_import_snapshot), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

private fun Modifier.optionalClick(enabled: Boolean, onClick: () -> Unit): Modifier =
    if (enabled) this.clickable(onClick = onClick) else this

@Composable
private fun Step1Content(
    name: String, onNameChange: (String) -> Unit,
    gender: String, onGenderChange: (String) -> Unit,
    avatarPath: String?, onAvatarChange: (String?) -> Unit,
    currentLang: AppLanguage, onLangChange: (AppLanguage) -> Unit
) {
    val context = LocalContext.current
    var cropUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val picker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { cropUri = it } }

    cropUri?.let { uri ->
        com.example.fitnesstracker.ui.components.ImageCropDialog(
            sourceUri = uri,
            onCancel = { cropUri = null },
            onCropped = { bmp ->
                val path = saveOnboardingAvatar(context, bmp)
                if (path != null) onAvatarChange(path)
                cropUri = null
            }
        )
    }

    Text(stringResource(R.string.onboarding_step1),
        style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)

    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(96.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .optionalClick(true) { picker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (avatarPath != null) {
                coil.compose.AsyncImage(
                    model = java.io.File(avatarPath),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(LeafIcons.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
            }
        }
    }

    Card(shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            com.example.fitnesstracker.ui.components.LanguageOptions(currentLang, onLangChange)

            AppTextField(value = name, onValueChange = { onNameChange(it) },
                label = stringResource(R.string.profile_name), modifier = Modifier.fillMaxWidth())

            Column {
                Text(stringResource(R.string.profile_gender), style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                val genders = listOf(
                    "male" to stringResource(R.string.profile_gender_male),
                    "female" to stringResource(R.string.profile_gender_female),
                    "other" to stringResource(R.string.profile_gender_other)
                )
                com.example.fitnesstracker.ui.components.LeafGenderChoices(gender, onGenderChange, genders)

            }
        }
    }
}

@Composable
private fun Step2Content(
    age: String, onAgeChange: (String) -> Unit,
    height: String, onHeightChange: (String) -> Unit,
    weight: String, onWeightChange: (String) -> Unit,
    bmi: Float, calories: Int
) {
    Text(stringResource(R.string.onboarding_step2),
        style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)

    Card(shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StepperDecimalField(value = age, onChange = onAgeChange,
                label = stringResource(R.string.profile_age),
                step = 1f, max = 100f,
                isError = age.isNotBlank() && (age.toIntOrNull() ?: 0) !in 5..100,
                supportingText = stringResource(R.string.profile_age_hint),
                modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StepperDecimalField(value = height, onChange = onHeightChange,
                    label = stringResource(R.string.profile_height),
                    step = 1f, max = 220f,
                    isError = height.isNotBlank() && (height.toFloatOrNull() ?: 0f) !in 100f..220f,
                    supportingText = stringResource(R.string.hint_height),
                    modifier = Modifier.weight(1f))
                StepperDecimalField(value = weight, onChange = onWeightChange,
                    label = stringResource(R.string.profile_weight),
                    step = 0.5f, max = 645f,
                    isError = weight.isNotBlank() && (weight.toFloatOrNull() ?: 0f) !in 20f..645f,
                    supportingText = stringResource(R.string.hint_weight),
                    modifier = Modifier.weight(1f))
            }
        }
    }

    if (bmi > 0) {
        Card(shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primary) {
                    Box(Modifier.size(44.dp), Alignment.Center) {
                        Icon(LeafIcons.MonitorWeight, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp))
                    }
                }
                Column {
                    Text("${"%.1f".format(bmi)} · ${bmiCategoryString(HealthCalc.bmiCategory(bmi))}",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    if (calories > 0) Text(stringResource(R.string.onboarding_calories, calories),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f))
                }
            }
        }
    }
}

@Composable
private fun Step3Content(
    chest: String, onChestChange: (String) -> Unit,
    waist: String, onWaistChange: (String) -> Unit,
    hips:  String, onHipsChange:  (String) -> Unit,
    bicep: String, onBicepChange: (String) -> Unit
) {
    Text(stringResource(R.string.onboarding_step3),
        style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
    Text(stringResource(R.string.profile_measurements_optional),
        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Card(shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Box(Modifier.size(36.dp), Alignment.Center) {
                        Icon(LeafIcons.Straighten, null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(18.dp))
                    }
                }
                Text(stringResource(R.string.profile_measurements),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(value = chest, onValueChange = onChestChange,
                    label = stringResource(R.string.profile_chest), modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                AppTextField(value = waist, onValueChange = onWaistChange,
                    label = stringResource(R.string.profile_waist), modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(value = hips, onValueChange = onHipsChange,
                    label = stringResource(R.string.profile_hips), modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                AppTextField(value = bicep, onValueChange = onBicepChange,
                    label = stringResource(R.string.profile_bicep), modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
            }
        }
    }
}

private fun genderIcon(g: String): ImageVector = when (g) {
    "male"   -> LeafIcons.Male
    "female" -> LeafIcons.Female
    else      -> LeafIcons.QuestionMark
}

private fun saveOnboardingAvatar(context: android.content.Context, bitmap: android.graphics.Bitmap): String? {
    return try {
        context.filesDir.listFiles { f -> f.name.startsWith("avatar_") }?.forEach { it.delete() }
        val file = java.io.File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}
