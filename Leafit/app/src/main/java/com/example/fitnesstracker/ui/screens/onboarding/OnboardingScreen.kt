package com.example.fitnesstracker.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.data.entities.UserProfile
import com.example.fitnesstracker.ui.components.AppTextField
import com.example.fitnesstracker.ui.components.StepperDecimalField
import com.example.fitnesstracker.utils.HealthCalc
import kotlin.math.roundToInt

/**
 * Екран первинного налаштування з валідацією:
 * вік 5–100, зріст 100–220 см, вага 20–645 кг.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onFinish: (UserProfile) -> Unit) {
    var name   by remember { mutableStateOf("") }
    var age    by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Чоловік") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    val haptic = LocalHapticFeedback.current
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    // Лагідна пульсація лого
    val logoPulse = rememberInfiniteTransition(label = "logoPulse")
    val logoScale by logoPulse.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "logoScale"
    )

    val ageI    = age.toIntOrNull() ?: 0
    val heightF = height.toFloatOrNull() ?: 0f
    val weightF = weight.toFloatOrNull() ?: 0f

    val ageValid    = ageI in 5..100
    val heightValid = heightF in 100f..220f
    val weightValid = weightF in 20f..645f

    val ageError    = age.isNotBlank() && !ageValid
    val heightError = height.isNotBlank() && !heightValid
    val weightError = weight.isNotBlank() && !weightValid

    val bmi      = if (heightValid && weightValid) HealthCalc.bmi(weightF, heightF) else 0f
    val calories = if (heightValid && weightValid && ageValid)
        HealthCalc.dailyCalories(weightF, heightF, ageI, gender) else 0f

    val isReady = name.isNotBlank() && ageValid && heightValid && weightValid

    Scaffold(containerColor = MaterialTheme.colorScheme.surface) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize().padding(padding).padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Векторний знак Leafit: без градієнта, м'який Material 3 контейнер
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .graphicsLayer { scaleX = logoScale; scaleY = logoScale }
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.58f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.EnergySavingsLeaf,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Leafit", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Налаштуйте профіль для м'якого старту",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(28.dp))

            // Біла картка з полями — плавна поява
            AnimatedVisibility(
                visible = appeared,
                enter = fadeIn(tween(450, 120)) + slideInVertically(tween(450, 120)) { it / 12 }
            ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(28.dp),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    AppTextField(
                        value = name, onValueChange = { name = it },
                        label = "Ім'я або нікнейм",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text("Стать", style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                listOf("Чоловік","Жінка").forEach { g ->
                                    FilterChip(selected = gender == g, onClick = { gender = g },
                                        label = { Text(g) }, shape = RoundedCornerShape(50.dp),
                                        leadingIcon = { Icon(genderIcon(g), null, modifier = Modifier.size(16.dp)) })
                                }
                            }
                            FilterChip(selected = gender == "Не хочу вказувати",
                                onClick = { gender = "Не хочу вказувати" },
                                label = { Text("Не хочу вказувати") }, shape = RoundedCornerShape(50.dp),
                                leadingIcon = { Icon(genderIcon("Не хочу вказувати"), null, modifier = Modifier.size(16.dp)) })
                        }
                    }

                    StepperDecimalField(
                        value = age,
                        onChange = { v -> age = v.filter { it.isDigit() }.take(3) },
                        label = "Вік (років)",
                        step = 1f, max = 100f,
                        isError = ageError,
                        supportingText = "Вік: від 5 до 100 років",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StepperDecimalField(
                            value = height,
                            onChange = { v -> height = v.filter { it.isDigit() || it == '.' }.take(5) },
                            label = "Зріст (см)",
                            step = 1f, max = 220f,
                            isError = heightError,
                            supportingText = "100–220 см",
                            modifier = Modifier.weight(1f)
                        )
                        StepperDecimalField(
                            value = weight,
                            onChange = { v -> weight = v.filter { it.isDigit() || it == '.' }.take(5) },
                            label = "Вага (кг)",
                            step = 0.5f, max = 645f,
                            isError = weightError,
                            supportingText = "20–645 кг",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            }

            Spacer(Modifier.height(16.dp))

            // Результат ІМТ
            AnimatedVisibility(visible = bmi > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(24.dp),
                    colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("ІМТ: ${"%.1f".format(bmi)} — ${HealthCalc.bmiCategory(bmi)}",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        if (calories > 0) {
                            Spacer(Modifier.height(4.dp))
                            Text("Денна норма: ${calories.roundToInt()} ккал",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = appeared,
                enter = fadeIn(tween(450, 260)) + slideInVertically(tween(450, 260)) { it / 12 }
            ) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (isReady) onFinish(UserProfile(
                        name = name.trim(), gender = gender, age = ageI,
                        heightCm = heightF, weightKg = weightF
                    ))
                },
                enabled = isReady,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text("Почати", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null)
            }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun genderIcon(g: String): ImageVector = when (g) {
    "Чоловік" -> Icons.Default.Male
    "Жінка"   -> Icons.Default.Female
    else      -> Icons.Default.QuestionMark
}
