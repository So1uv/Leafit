package com.example.fitnesstracker.ui.screens.nutrition

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.data.entities.Meal
import com.example.fitnesstracker.ui.components.AppTextField
import com.example.fitnesstracker.ui.components.bouncyClick
import com.example.fitnesstracker.ui.components.GlowProgressBar
import com.example.fitnesstracker.ui.components.LargeScreenTitle
import com.example.fitnesstracker.ui.components.MetricValueWithSuffix
import com.example.fitnesstracker.ui.components.StepperDecimalField
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.viewmodel.NutritionViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(vm: NutritionViewModel) {
    val meals        by vm.meals.collectAsState()
    val selectedDay  by vm.selectedDay.collectAsState()
    val goal         by vm.dailyCalorieGoal.collectAsState()
    val totalCal     by vm.totalCalories.collectAsState()
    val proteins     by vm.totalProteins.collectAsState()
    val fats         by vm.totalFats.collectAsState()
    val carbs        by vm.totalCarbs.collectAsState()
    val dates        by vm.availableDates.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var expandedType   by remember { mutableStateOf<String?>(null) }

    // Великий заголовок у списку + поява компактного у топбарі при скролі (iOS large title)
    val listState = rememberLazyListState()
    val collapsedTitle by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 24 }
    }
    val topTitleAlpha by animateFloatAsState(if (collapsedTitle) 1f else 0f, tween(180), label = "topTitle")
    val topBarColor by animateColorAsState(
        if (collapsedTitle) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface,
        tween(220), label = "topBarBg"
    )
    val mealTypes = listOf("Сніданок", "Обід", "Перекус", "Вечеря")

    if (showDatePicker) {
        DateSelectDialog(
            dates = dates,
            selectedDay = selectedDay,
            onSelect = { vm.selectDay(it); showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LargeScreenTitle("Харчування", icon = Icons.Default.Restaurant)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.bouncyClick { showDatePicker = true }
                        ) {
                            Row(
                                Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CalendarMonth, null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (selectedDay == DateUtils.todayStart()) "сьогодні"
                                    else DateUtils.formatDate(selectedDay),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = topBarColor)
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                NutritionSummaryCard(
                    totalCal = totalCal,
                    goal = goal,
                    proteins = proteins,
                    fats = fats,
                    carbs = carbs
                )
            }

            mealTypes.forEach { type ->
                val typeMeals = meals.filter { it.mealType == type }
                item(key = type) {
                    MealSection(
                        type = type,
                        meals = typeMeals,
                        expanded = expandedType == type,
                        onToggle = { expandedType = if (expandedType == type) null else type },
                        onAdd = { vm.addMeal(it) },
                        onDelete = { vm.deleteMeal(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionSummaryCard(totalCal: Float, goal: Float, proteins: Float, fats: Float, carbs: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            val animCal by animateFloatAsState(totalCal, tween(800, easing = EaseOutCubic), label = "nutCal")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Restaurant, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.size(10.dp))
                    Text("Калорії", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                MetricValueWithSuffix(
                    value = "${animCal.roundToInt()} / ${goal.roundToInt()}",
                    suffix = " ккал",
                    valueStyle = MaterialTheme.typography.titleMedium,
                    suffixStyle = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    suffixColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(10.dp))
            GlowProgressBar(
                progress = (totalCal / goal).coerceIn(0f, 1f),
                color = MaterialTheme.colorScheme.primary,
                height = 8.dp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (totalCal < goal) "залишилось ${(goal - totalCal).roundToInt()} ккал" else "денну ціль досягнуто",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroItem("Білки", proteins, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                MacroItem("Жири", fats, MaterialTheme.colorScheme.error, Modifier.weight(1f))
                MacroItem("Вугл.", carbs, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MacroItem(label: String, value: Float, color: Color, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        MetricValueWithSuffix(
            value = "%.0f".format(value),
            suffix = " г",
            valueStyle = MaterialTheme.typography.titleSmall,
            suffixStyle = MaterialTheme.typography.labelSmall,
            color = color
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MealSection(
    type: String,
    meals: List<Meal>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAdd: (Meal) -> Unit,
    onDelete: (Meal) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, tween(250), label = "arrow")

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(tween(240, easing = EaseOutCubic)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggle()
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (mealIcon, mealTint) = when (type) {
                    "Сніданок" -> Icons.Default.WbSunny to MaterialTheme.colorScheme.tertiary
                    "Обід"     -> Icons.Default.LunchDining to MaterialTheme.colorScheme.primary
                    "Перекус"  -> Icons.Default.BakeryDining to MaterialTheme.colorScheme.secondary
                    "Вечеря"   -> Icons.Default.DinnerDining to MaterialTheme.colorScheme.error
                    else       -> Icons.Default.Restaurant to MaterialTheme.colorScheme.primary
                }
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(15.dp))
                        .background(mealTint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(mealIcon, null, tint = mealTint, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(type, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (meals.isNotEmpty()) "${meals.sumOf { it.calories.toDouble() }.toInt()} ккал · ${meals.size} страв" else "Відкрийте, щоб додати страву",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.ExpandMore,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(arrowRotation)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(
                        Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                    if (meals.isEmpty()) {
                        Text(
                            "Поки що немає записів",
                            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        meals.forEach { meal ->
                            MealRow(meal = meal, onDelete = onDelete)
                        }
                    }
                    InlineAddMealForm(type = type, onAdd = onAdd)
                }
            }
        }
    }
}

@Composable
private fun MealRow(meal: Meal, onDelete: (Meal) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(meal.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "${meal.calories.roundToInt()} ккал · Б${meal.proteins.toInt()} Ж${meal.fats.toInt()} В${meal.carbs.toInt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { onDelete(meal) }) {
            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun InlineAddMealForm(type: String, onAdd: (Meal) -> Unit) {
    var name     by remember(type) { mutableStateOf("") }
    var calories by remember(type) { mutableStateOf("") }
    var proteins by remember(type) { mutableStateOf("") }
    var fats     by remember(type) { mutableStateOf("") }
    var carbs    by remember(type) { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.72f)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.size(8.dp))
                Text("Додати у $type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Назва страви",
                modifier = Modifier.fillMaxWidth()
            )
            StepperDecimalField(
                value = calories,
                onChange = { calories = it.filter { c -> c.isDigit() || c == '.' } },
                label = "Калорії (ккал)",
                step = 10f,
                max = 5000f,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = proteins,
                    onValueChange = { proteins = it.filter { c -> c.isDigit() || c == '.' } },
                    label = "Білки",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    value = fats,
                    onValueChange = { fats = it.filter { c -> c.isDigit() || c == '.' } },
                    label = "Жири",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    value = carbs,
                    onValueChange = { carbs = it.filter { c -> c.isDigit() || c == '.' } },
                    label = "Вугл.",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onAdd(
                                Meal(
                                    name = name.trim(),
                                    mealType = type,
                                    calories = calories.toFloatOrNull() ?: 0f,
                                    proteins = proteins.toFloatOrNull() ?: 0f,
                                    fats = fats.toFloatOrNull() ?: 0f,
                                    carbs = carbs.toFloatOrNull() ?: 0f
                                )
                            )
                            name = ""
                            calories = ""
                            proteins = ""
                            fats = ""
                            carbs = ""
                        }
                    },
                    enabled = name.isNotBlank(),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text("Додати")
                }
                OutlinedButton(
                    onClick = {
                        name = ""
                        calories = ""
                        proteins = ""
                        fats = ""
                        carbs = ""
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).height(46.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("Очистити")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelectDialog(dates: List<Long>, selectedDay: Long, onSelect: (Long) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = selectedDay)
    val pickerColors = DatePickerDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
        todayDateBorderColor = MaterialTheme.colorScheme.primary,
        todayContentColor = MaterialTheme.colorScheme.primary,
        weekdayContentColor = MaterialTheme.colorScheme.primary
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        colors = pickerColors,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onSelect(it) }
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } }
    ) {
        DatePicker(
            state = state,
            colors = pickerColors,
            title = {
                Text("Виберіть дату",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp))
            },
            headline = {
                Text(
                    java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale("uk")).format(
                        state.selectedDateMillis ?: System.currentTimeMillis()
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
                )
            }
        )
    }
}
