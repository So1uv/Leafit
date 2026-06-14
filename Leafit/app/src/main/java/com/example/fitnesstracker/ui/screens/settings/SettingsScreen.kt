package com.example.fitnesstracker.ui.screens.settings

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.fitnesstracker.ui.components.AppTextField
import com.example.fitnesstracker.ui.components.ExpressiveDialog
import com.example.fitnesstracker.ui.components.LargeScreenTitle
import com.example.fitnesstracker.ui.components.StepperDecimalField
import com.example.fitnesstracker.ui.components.bouncyClick
import java.io.File
import com.example.fitnesstracker.data.entities.Achievement
import com.example.fitnesstracker.data.entities.UserProfile
import com.example.fitnesstracker.utils.HealthCalc
import com.example.fitnesstracker.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

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
    val collapsedTitle by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 24 }
    }
    val topTitleAlpha by androidx.compose.animation.core.animateFloatAsState(
        if (collapsedTitle) 1f else 0f,
        androidx.compose.animation.core.tween(180), label = "topTitle"
    )
    val topBarColor by androidx.compose.animation.animateColorAsState(
        if (collapsedTitle) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface,
        androidx.compose.animation.core.tween(220), label = "topBarBg"
    )

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Копіюємо фото у внутрішнє сховище — content:// URI тимчасовий і губиться після перезапуску
            val savedPath = copyAvatarToInternalStorage(context, it)
            if (savedPath != null) vm.updateAvatar(savedPath)
        }
    }

    if (showEditDialog && profile != null) {
        EditProfileDialog(profile = profile!!, onSave = { vm.saveProfile(it); showEditDialog = false }, onDismiss = { showEditDialog = false })
    }
    if (showAchievements) {
        AchievementsListDialog(achievements = achievements, onDismiss = { showAchievements = false })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        LargeScreenTitle("Профіль", icon = Icons.Default.Person)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = topBarColor),
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад") } }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {


            // Аватар + ім'я
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        // Аватар у м'якому однотонному кільці
                        Box(
                            modifier = Modifier.size(98.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                        Box(
                            modifier = Modifier.size(91.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                        Box(
                            modifier = Modifier.size(85.dp).clip(CircleShape)
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
                                    style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Натисніть для зміни фото", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(profile?.name ?: "—", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        profile?.let { p ->
                            Text("${p.age} р · ${p.weightKg} кг · ${p.heightCm} см",
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val bmi = HealthCalc.bmi(p.weightKg, p.heightCm)
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.clip(RoundedCornerShape(50.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                Text("ІМТ ${"%.1f".format(bmi)} · ${HealthCalc.bmiCategory(bmi)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }

            // Статистика профілю
            item {
                val unlockedCount = achievements.count { it.unlockedAt != null }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileStatTile(Modifier.weight(1f), "$workoutCount", "тренувань", MaterialTheme.colorScheme.primary)
                    ProfileStatTile(Modifier.weight(1f), "$sleepCount", "записів сну", MaterialTheme.colorScheme.tertiary)
                    ProfileStatTile(Modifier.weight(1f), "$unlockedCount", "досягнень", MaterialTheme.colorScheme.error)
                }
            }

            // Редагувати
            item {
                FilledTonalButton(onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(50)) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Редагувати профіль")
                }
            }

            // Досягнення
            item {
                val unlocked = achievements.count { it.unlockedAt != null }
                Card(modifier = Modifier.fillMaxWidth().bouncyClick { showAchievements = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) {
                            Icon(Icons.Default.EmojiEvents, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Досягнення", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("$unlocked / ${achievements.size} отримано", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
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

    val ageInt   = age.toIntOrNull()
    val heightF  = height.toFloatOrNull()
    val weightF  = weight.toFloatOrNull()

    val nameOk   = name.trim().isNotEmpty()
    val ageOk    = ageInt != null && ageInt in 5..100
    val heightOk = heightF != null && heightF in 100f..220f
    val weightOk = weightF != null && weightF in 20f..645f
    val canSave  = nameOk && ageOk && heightOk && weightOk

    ExpressiveDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редагувати профіль") },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(value = name, onValueChange = { name = it.take(40) }, label = "Ім'я",
                    modifier = Modifier.fillMaxWidth())
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Чоловік","Жінка").forEach { g ->
                            FilterChip(selected = gender == g, onClick = { gender = g }, label = { Text(g) }, shape = RoundedCornerShape(50.dp),
                                leadingIcon = { Icon(genderIcon(g), null, modifier = Modifier.size(16.dp)) })
                        }
                    }
                    FilterChip(selected = gender == "Не хочу вказувати", onClick = { gender = "Не хочу вказувати" },
                        label = { Text("Не хочу вказувати") }, shape = RoundedCornerShape(50.dp),
                        leadingIcon = { Icon(genderIcon("Не хочу вказувати"), null, modifier = Modifier.size(16.dp)) })
                }
                StepperDecimalField(
                    value = age,
                    onChange = { age = it.filter { c -> c.isDigit() }.take(3) },
                    label = "Вік", step = 1f, max = 100f,
                    isError = age.isNotEmpty() && !ageOk,
                    supportingText = "5–100 років",
                    modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StepperDecimalField(
                        value = height,
                        onChange = { height = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                        label = "Зріст (см)", step = 1f, max = 220f,
                        isError = height.isNotEmpty() && !heightOk,
                        supportingText = "100–220 см",
                        modifier = Modifier.weight(1f))
                    StepperDecimalField(
                        value = weight,
                        onChange = { weight = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                        label = "Вага (кг)", step = 0.5f, max = 645f,
                        isError = weight.isNotEmpty() && !weightOk,
                        supportingText = "20–645 кг",
                        modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                enabled = canSave,
                onClick = { onSave(profile.copy(
                    name = name.trim(), gender = gender,
                    age = ageInt ?: profile.age,
                    heightCm = heightF ?: profile.heightCm,
                    weightKg = weightF ?: profile.weightKg)) },
                shape = RoundedCornerShape(50)
            ) { Text("Зберегти") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } }
    )
}

@Composable
private fun AchievementsListDialog(achievements: List<Achievement>, onDismiss: () -> Unit) {
    ExpressiveDialog(
        onDismissRequest = onDismiss,
        title = { Text("Всі досягнення") },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                achievements.forEach { a ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (a.unlockedAt != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                            Alignment.Center) {
                            Icon(Icons.Default.EmojiEvents, null,
                                tint = if (a.unlockedAt != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text(a.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
                                color = if (a.unlockedAt != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline)
                            Text(a.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрити") } }
    )
}

/**
 * Копіює вибране зображення у внутрішнє сховище застосунку.
 * Повертає абсолютний шлях до файлу або null при помилці.
 */
private fun copyAvatarToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        // Видаляємо старі аватари, щоб не накопичувались
        context.filesDir.listFiles { f -> f.name.startsWith("avatar_") }?.forEach { it.delete() }
        val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
        val input = context.contentResolver.openInputStream(uri) ?: return null
        input.use { inp -> file.outputStream().use { out -> inp.copyTo(out) } }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun ProfileStatTile(modifier: Modifier, value: String, label: String, color: Color) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun genderIcon(g: String): ImageVector = when (g) {
    "Чоловік" -> Icons.Default.Male
    "Жінка"   -> Icons.Default.Female
    else      -> Icons.Default.QuestionMark
}
