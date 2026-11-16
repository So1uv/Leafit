package com.example.fitnesstracker.ui.screens.onboarding

import com.example.fitnesstracker.ui.components.LeafFilledTonalIconButton as FilledTonalIconButton
import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

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
import androidx.compose.ui.graphics.graphicsLayer
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
            SetupProgress(step, steps) { previous ->
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
                            height = height, onHeightChange = { height = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' }.take(5) },
                            weight = weight, onWeightChange = { weight = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' }.take(5) },
                            bmi = if (heightF in 100f..220f && weightF in 20f..645f) HealthCalc.bmi(weightF, heightF) else 0f,
                            calories = if (ageI in 5..100 && heightF in 100f..220f && weightF in 20f..645f)
                                HealthCalc.dailyCalories(weightF, heightF, ageI, gender).roundToInt() else 0
                        )
                        2 -> Step3Content(
                            chest = chest, onChestChange = { chest = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' }.take(5) },
                            waist = waist, onWaistChange = { waist = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' }.take(5) },
                            hips  = hips,  onHipsChange  = { hips  = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' }.take(5) },
                            bicep = bicep, onBicepChange = { bicep = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' }.take(5) }
                        )
                    }
                }
            }
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            .onSizeChanged { footerHeightPx = it.height }
            .navigationBarsPadding().padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 12.dp)
            .background(palette.surfaceContainerHigh, RoundedCornerShape(36.dp)).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (importError) Text(stringResource(R.string.onboarding_import_error),
                style = MaterialTheme.typography.bodySmall, color = palette.error)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (step > 0) FilledTonalIconButton(enabled = !importing, onClick = { step-- },
                    modifier = Modifier.size(64.dp)) {
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
                    }, modifier = Modifier.weight(1f).heightIn(min = 64.dp)
                ) {
                    Text(stringResource(if (step < 2) R.string.common_continue_next else R.string.common_start),
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(10.dp))
                    Icon(if (step < 2) com.example.fitnesstracker.ui.components.LeafIcons.ArrowForward
                        else com.example.fitnesstracker.ui.components.LeafIcons.Check, null, Modifier.size(20.dp))
                }
            }
            if (step == 0) TextButton(enabled = !importing,
                onClick = { snapshotPicker.launch("application/json") }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                if (importing) com.example.fitnesstracker.ui.components.LeafLoadingIndicator(Modifier.size(22.dp))
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

    SetupHero(LeafIcons.Eco, stringResource(R.string.setup_intro), stringResource(R.string.setup_intro_hint))

    val colors = MaterialTheme.colorScheme
    Surface(onClick = { picker.launch("image/*") }, shape = RoundedCornerShape(32.dp), color = colors.surfaceContainer,
        modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(96.dp), contentAlignment = Alignment.BottomEnd) {
                Box(Modifier.fillMaxSize().clip(com.example.fitnesstracker.ui.components.LeafRosette(8)).background(colors.primaryContainer),
                    contentAlignment = Alignment.Center) {
                    if (avatarPath != null) coil.compose.AsyncImage(model = java.io.File(avatarPath), contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                    else Text(name.trim().take(1).uppercase().ifEmpty { "?" }, style = MaterialTheme.typography.displaySmall,
                        color = colors.onPrimaryContainer)
                }
                Box(Modifier.size(36.dp).background(colors.primary, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(LeafIcons.AddAPhoto, null, tint = colors.onPrimary, modifier = Modifier.size(18.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.b16_photo), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.b16_photo_hint), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Icon(LeafIcons.ChevronRight, null, tint = colors.onSurfaceVariant)
        }
    }

    Surface(shape = RoundedCornerShape(32.dp), color = colors.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            com.example.fitnesstracker.ui.components.LeafCardHeader(LeafIcons.Person, stringResource(R.string.b16_about_you))
            AppTextField(value = name, onValueChange = { onNameChange(it) },
                label = stringResource(R.string.profile_name), modifier = Modifier.fillMaxWidth())
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.profile_gender), style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                val genders = listOf(
                    "male" to stringResource(R.string.profile_gender_male),
                    "female" to stringResource(R.string.profile_gender_female),
                    "other" to stringResource(R.string.profile_gender_other)
                )
                com.example.fitnesstracker.ui.components.LeafGenderChoices(gender, onGenderChange, genders)
            }
        }
    }

    Surface(shape = RoundedCornerShape(32.dp), color = colors.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            com.example.fitnesstracker.ui.components.LeafCardHeader(LeafIcons.Language, stringResource(R.string.settings_language),
                tone = com.example.fitnesstracker.ui.components.LeafTone.Secondary)
            com.example.fitnesstracker.ui.components.LanguageOptions(currentLang, onLangChange)
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
    SetupHero(LeafIcons.MonitorWeight, stringResource(R.string.onboarding_step2), stringResource(R.string.setup_body_hint))

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        SetupStepper(LeafIcons.Cake, stringResource(R.string.profile_age), age, onAgeChange, 1f, 5f..100f, false, 25f, 0,
            stringResource(R.string.profile_age_hint))
        SetupStepper(LeafIcons.Height, stringResource(R.string.profile_height), height, onHeightChange, 1f, 100f..220f, false, 170f, 1,
            stringResource(R.string.hint_height))
        SetupStepper(LeafIcons.MonitorWeight, stringResource(R.string.profile_weight), weight, onWeightChange, .5f, 20f..645f, true, 70f, 2,
            stringResource(R.string.hint_weight))
    }

    androidx.compose.animation.AnimatedVisibility(bmi > 0) {
        val colors = MaterialTheme.colorScheme
        Surface(shape = RoundedCornerShape(32.dp), color = colors.primaryContainer, contentColor = colors.onPrimaryContainer,
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.b16_bmi), style = MaterialTheme.typography.labelLarge)
                        Text("%.1f".format(bmi), style = MaterialTheme.typography.displaySmall)
                    }
                    Surface(shape = CircleShape, color = colors.primary, contentColor = colors.onPrimary) {
                        Text(bmiCategoryString(HealthCalc.bmiCategory(bmi)), Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge)
                    }
                }
                val marker by animateFloatAsState(((bmi - 15f) / 25f).coerceIn(0f, 1f), spring(.7f, 200f), label = "bmiMarker")
                val zones = listOf(3.5f to colors.tertiary, 6.5f to colors.primary, 5f to colors.secondary, 10f to colors.error)
                val ink = colors.onPrimaryContainer
                androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(28.dp)) {
                    val gap = 3.dp.toPx()
                    val bar = 12.dp.toPx()
                    val top = size.height - bar
                    var x = 0f
                    zones.forEach { (span, tone) ->
                        val width = size.width * span / 25f
                        drawRoundRect(tone, androidx.compose.ui.geometry.Offset(x, top),
                            androidx.compose.ui.geometry.Size((width - gap).coerceAtLeast(1f), bar),
                            androidx.compose.ui.geometry.CornerRadius(bar / 2f))
                        x += width
                    }
                    drawCircle(ink, 7.dp.toPx(), androidx.compose.ui.geometry.Offset(size.width * marker, 7.dp.toPx()))
                }
                if (calories > 0) Surface(shape = RoundedCornerShape(20.dp), color = colors.surface.copy(alpha = .6f),
                    contentColor = colors.onSurface, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(LeafIcons.LocalFireDepartment, null, Modifier.size(22.dp), tint = colors.primary)
                        Text(stringResource(R.string.onboarding_calories, calories), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupStepper(icon: ImageVector, label: String, value: String, onChange: (String) -> Unit, step: Float,
    range: ClosedFloatingPointRange<Float>, decimal: Boolean, fallback: Float, index: Int, hint: String) {
    val colors = MaterialTheme.colorScheme
    val number = value.toFloatOrNull()
    val error = value.isNotBlank() && (number == null || number !in range)
    fun shift(by: Float) {
        val next = ((number ?: fallback) + by).coerceIn(range.start, range.endInclusive)
        onChange(if (decimal) "%.1f".format(java.util.Locale.US, next) else next.roundToInt().toString())
    }
    Surface(shape = RoundedCornerShape(topStart = if (index == 0) 32.dp else 8.dp, topEnd = if (index == 0) 32.dp else 8.dp,
        bottomStart = if (index == 2) 32.dp else 8.dp, bottomEnd = if (index == 2) 32.dp else 8.dp),
        color = if (error) colors.errorContainer else colors.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, end = 12.dp, top = 14.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                com.example.fitnesstracker.ui.components.LeafIconBadge(icon,
                    size = com.example.fitnesstracker.ui.components.LeafIconSize.Small)
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.titleSmall)
                    Text(hint, style = MaterialTheme.typography.bodySmall, color = if (error) colors.error else colors.onSurfaceVariant,
                        maxLines = 2)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalIconButton(onClick = { shift(-step) }, modifier = Modifier.size(56.dp)) {
                    Icon(LeafIcons.Remove, null)
                }
                androidx.compose.foundation.text.BasicTextField(value = value, onValueChange = onChange, singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.displaySmall.copy(color = if (error) colors.error else colors.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.primary),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.Center) {
                            if (value.isEmpty()) Text("—", style = MaterialTheme.typography.displaySmall, color = colors.outline)
                            inner()
                        }
                    })
                FilledTonalIconButton(onClick = { shift(step) }, modifier = Modifier.size(56.dp)) {
                    Icon(LeafIcons.Add, null)
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
    SetupHero(LeafIcons.Straighten, stringResource(R.string.onboarding_step3), stringResource(R.string.setup_measure_hint))

    val colors = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(24.dp), color = colors.tertiaryContainer, contentColor = colors.onTertiaryContainer,
        modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(LeafIcons.Lightbulb, null, Modifier.size(22.dp))
            Text(stringResource(R.string.b16_optional), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        }
    }
    Surface(shape = RoundedCornerShape(32.dp), color = colors.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            com.example.fitnesstracker.ui.components.LeafCardHeader(LeafIcons.Straighten, stringResource(R.string.profile_measurements),
                tone = com.example.fitnesstracker.ui.components.LeafTone.Secondary)
            listOf(listOf(Triple(R.string.profile_chest, chest, onChestChange), Triple(R.string.profile_waist, waist, onWaistChange)),
                listOf(Triple(R.string.profile_hips, hips, onHipsChange), Triple(R.string.profile_bicep, bicep, onBicepChange))).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { (label, value, update) ->
                        AppTextField(value, update, stringResource(label), Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                    }
                }
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

@Composable
private fun SetupHero(icon: ImageVector, title: String, subtitle: String) {
    val colors = MaterialTheme.colorScheme
    val spin by rememberInfiniteTransition(label = "setupHero").animateFloat(0f, 360f,
        infiniteRepeatable(tween(18000, easing = LinearEasing)), label = "setupSpin")
    val pop = remember { Animatable(.6f) }
    LaunchedEffect(icon) { pop.snapTo(.6f); pop.animateTo(1f, spring(.5f, 300f)) }
    Column(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(120.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value }, contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxSize().graphicsLayer { rotationZ = spin }
                .clip(com.example.fitnesstracker.ui.components.LeafRosette(8)).background(colors.primary))
            Icon(icon, null, Modifier.size(52.dp), tint = colors.onPrimary)
        }
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.headlineLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun SetupProgress(step: Int, steps: List<String>, onBack: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.b16_step, step + 1, steps.size), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge,
                color = colors.primary)
            Text(steps.getOrElse(step) { "" }, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            steps.forEachIndexed { index, _ ->
                val weight by animateFloatAsState(if (index == step) 2.4f else 1f, spring(.7f, 300f), label = "setupStep")
                val tone by androidx.compose.animation.animateColorAsState(when {
                    index < step -> colors.primary
                    index == step -> colors.primary
                    else -> colors.surfaceContainerHighest
                }, label = "setupTone")
                Box(Modifier.weight(weight).height(if (index == step) 14.dp else 10.dp).clip(CircleShape).background(tone)
                    .optionalClick(index < step) { onBack(index) })
            }
        }
    }
}
