package com.example.fitnesstracker.ui.screens.nutrition

import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

import com.example.fitnesstracker.ui.components.collectAsLeafState
import com.example.fitnesstracker.ui.components.LeafScreenScaffold
import com.example.fitnesstracker.ui.components.rememberLeafHeaderState
import com.example.fitnesstracker.ui.theme.LeafCard as Card
import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import com.example.fitnesstracker.ui.components.LeafEditSheet
import com.example.fitnesstracker.ui.components.LeafTonalButton
import com.example.fitnesstracker.ui.components.LeafButton
import com.example.fitnesstracker.ui.components.LeafIcons
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.fitnesstracker.data.entities.Meal
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.AppTextField
import com.example.fitnesstracker.ui.components.bouncyClick
import androidx.compose.ui.platform.LocalContext
import com.example.fitnesstracker.ui.components.RollingNumber
import com.example.fitnesstracker.ui.components.GlowProgressBar
import com.example.fitnesstracker.ui.components.MetricValueWithSuffix
import com.example.fitnesstracker.ui.components.StepperDecimalField
import com.example.fitnesstracker.viewmodel.NutritionViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun NutritionScreen(vm: NutritionViewModel) {
    val meals        by vm.meals.collectAsLeafState()
    val profile      by vm.profile.collectAsLeafState()
    val selectedDay  by vm.selectedDay.collectAsLeafState()
    val goal         by vm.dailyCalorieGoal.collectAsLeafState()
    val totalCal     by vm.totalCalories.collectAsLeafState()
    val proteins     by vm.totalProteins.collectAsLeafState()
    val fats         by vm.totalFats.collectAsLeafState()
    val carbs        by vm.totalCarbs.collectAsLeafState()
    val dates        by vm.availableDates.collectAsLeafState()
    val waterMl      by vm.waterMlToday.collectAsLeafState()
    val waterGoal    by vm.waterGoalMl.collectAsLeafState()
    val savedMeals   by vm.savedMeals.collectAsLeafState()
    val products     by vm.products.collectAsLeafState()
    val mealsByType = remember(meals) { meals.groupBy { it.mealType } }
    val context      = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    var expandedType   by rememberSaveable { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val mealTypes = listOf(stringResource(R.string.nutrition_breakfast), stringResource(R.string.nutrition_lunch), stringResource(R.string.nutrition_snack), stringResource(R.string.nutrition_dinner))

    if (showDatePicker) {
        DateSelectDialog(
            dates = dates,
            selectedDay = selectedDay,
            onSelect = { vm.selectDay(it); showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }

    val headerState = rememberLeafHeaderState()
    LeafScreenScaffold(
        headerState = headerState,
        topBar = {
            com.example.fitnesstracker.ui.components.DayPageTopBar(
                todayTitle = stringResource(R.string.nutrition_day_today),
                dayTitle = stringResource(R.string.nutrition_day_other),
                selectedDay = selectedDay, onChooseDate = { showDatePicker = true }, headerState = headerState)
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()

                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 12.dp, bottom = 120.dp)
        ) {



            item(key = "nutrition_summary", contentType = "nutrition_summary") {
                NutritionSummaryCard(
                    totalCal = totalCal,
                    goal = goal,
                    proteins = proteins,
                    fats = fats,
                    carbs = carbs,
                    recordsCount = meals.size,
                    hasProfile = profile != null
                )
            }

            item(key = "hydration", contentType = "hydration") {
                com.example.fitnesstracker.ui.components.HydrationCard(
                    waterMl = waterMl,
                    goalMl = waterGoal,
                    onAdd = { vm.addWater(it) },
                    onRemove = { vm.removeWater() }
                )
            }

            if (savedMeals.isNotEmpty()) {
                item(key = "saved_meals", contentType = "saved_meals") {
                    SavedMealsRow(
                        saved = savedMeals,
                        onAdd = { saved, factor ->
                            val targetType = expandedType ?: mealTypes.first()
                            vm.addFromSavedScaled(saved, targetType, factor)
                        },
                        onRemove = { vm.deleteSavedMeal(it) }
                    )
                }
            }

            item(key = "meals_header", contentType = "meals_header") {
                com.example.fitnesstracker.ui.components.HistorySectionLabel(
                    stringResource(R.string.nutrition_meals_section), meals.size, LeafIcons.Restaurant)
            }

            mealTypes.forEach { type ->
                val typeMeals = mealsByType[type].orEmpty()
                item(key = type, contentType = "meal_section") {
                    MealSection(
                        type = type,
                        meals = typeMeals,
                        expanded = expandedType == type,
                        onToggle = { expandedType = if (expandedType == type) null else type },
                        onAdd = { vm.addMeal(it) },
                        onDelete = { vm.deleteMeal(it) },
                        onEdit = { vm.updateMeal(it) },
                        onSaveTemplate = { vm.saveMealTemplate(it) },
                        products = products,
                        onCreateProduct = { vm.addProduct(it) },
                        onDeleteProduct = { vm.deleteProduct(it) },
                        onAddFromProduct = { p, grams -> vm.addFromProduct(p, grams, type) }
                    )
                }
            }

            item(key = "recipes", contentType = "recipes") {
                RecipePlannerCard(selectedDay, mealTypes, products, vm::addPlannedMeal)
            }


        }
    }
}

@Composable
private fun NutritionSummaryCard(totalCal: Float, goal: Float, proteins: Float, fats: Float, carbs: Float,
    recordsCount: Int, hasProfile: Boolean) {
    val colors = MaterialTheme.colorScheme
    val consumed = totalCal.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    val target = goal.takeIf { it.isFinite() && it > 0f } ?: 1f
    val over = consumed > target
    val fraction = (consumed / target).coerceIn(0f, 1f)
    val animated = androidx.compose.animation.core.animateFloatAsState(fraction,
        androidx.compose.animation.core.spring(dampingRatio = .8f, stiffness = 120f), label = "nutritionProgress")
    val locale = com.example.fitnesstracker.ui.components.appLocale()
    val numberFormat = remember(locale) { java.text.NumberFormat.getIntegerInstance(locale) }
    val arcColor = if (over) colors.tertiary else colors.primary
    val track = colors.onPrimaryContainer.copy(alpha = .12f)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        com.example.fitnesstracker.ui.theme.LeafSurface(shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp,
            bottomStart = 8.dp, bottomEnd = 8.dp), color = colors.primaryContainer, contentColor = colors.onPrimaryContainer) {
            Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.nutrition_energy_title), Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium)
                    com.example.fitnesstracker.ui.components.MetricExplanation(
                        stringResource(R.string.nutrition_calories_label),
                        listOf(stringResource(R.string.metric_meals_count, recordsCount),
                            stringResource(R.string.metric_calorie_formula)),
                        if (hasProfile) null else stringResource(R.string.metric_profile_missing))
                }
                Row(Modifier.padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Box(Modifier.size(124.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                            val stroke = 14.dp.toPx()
                            val inset = stroke / 2f
                            val arc = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
                            val origin = androidx.compose.ui.geometry.Offset(inset, inset)
                            drawArc(track, 135f, 270f, false, origin, arc,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                            if (animated.value > .005f) drawArc(arcColor, 135f, 270f * animated.value, false, origin, arc,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            com.example.fitnesstracker.ui.components.LeafIconBadge(LeafIcons.LocalFireDepartment,
                                size = com.example.fitnesstracker.ui.components.LeafIconSize.Small,
                                tone = com.example.fitnesstracker.ui.components.LeafTone.Solid,
                                shape = com.example.fitnesstracker.ui.components.LeafRosette(8))
                            Text("${(consumed * 100f / target).roundToInt().coerceIn(0, 999)}%", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(stringResource(R.string.b6_consumed), style = MaterialTheme.typography.labelLarge)
                        com.example.fitnesstracker.ui.components.LeafFitText(numberFormat.format(consumed.roundToInt()),
                            style = MaterialTheme.typography.displayMedium.copy(fontFamily = com.example.fitnesstracker.ui.theme.NumericFontFamily))
                        Text(stringResource(R.string.unit_kcal), style = MaterialTheme.typography.titleSmall)
                    }
                }
                Row(Modifier.fillMaxWidth().padding(end = 8.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    EnergyStat(stringResource(if (over) R.string.b12_energy_over else R.string.b12_energy_left),
                        numberFormat.format(kotlin.math.abs(target - consumed).roundToInt()), 0, Modifier.weight(1f))
                    EnergyStat(stringResource(R.string.b12_energy_goal), numberFormat.format(goal.roundToInt()), 1, Modifier.weight(1f))
                    EnergyStat(stringResource(R.string.b12_energy_meals), recordsCount.toString(), 2, Modifier.weight(1f))
                }
            }
        }
        val energy = listOf(proteins * 4f, fats * 9f, carbs * 4f).map { it.takeIf { v -> v.isFinite() && v > 0f } ?: 0f }
        val total = energy.sum()
        val tones = listOf(colors.primary, colors.secondary, colors.tertiary)
        if (total > 0f) Row(Modifier.fillMaxWidth().height(12.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            energy.forEachIndexed { index, value ->
                if (value > 0f) Box(Modifier.weight(value / total).fillMaxHeight().background(tones[index], RoundedCornerShape(6.dp)))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            MacroItem(stringResource(R.string.nutrition_proteins), proteins, colors.primary, Modifier.weight(1f), 0,
                if (total > 0f) (energy[0] * 100f / total).roundToInt() else null)
            MacroItem(stringResource(R.string.nutrition_fats), fats, colors.secondary, Modifier.weight(1f), 1,
                if (total > 0f) (energy[1] * 100f / total).roundToInt() else null)
            MacroItem(stringResource(R.string.nutrition_carbs), carbs, colors.tertiary, Modifier.weight(1f), 2,
                if (total > 0f) (energy[2] * 100f / total).roundToInt() else null)
        }
    }
}

@Composable
private fun EnergyStat(label: String, value: String, index: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    com.example.fitnesstracker.ui.theme.LeafSurface(modifier, shape = RoundedCornerShape(
        topStart = if (index == 0) 20.dp else 6.dp, bottomStart = if (index == 0) 20.dp else 6.dp,
        topEnd = if (index == 2) 20.dp else 6.dp, bottomEnd = if (index == 2) 20.dp else 6.dp),
        color = colors.surface.copy(alpha = .55f), contentColor = colors.onSurface) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            com.example.fitnesstracker.ui.components.LeafFitText(value, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MacroItem(label: String, value: Float, color: Color, modifier: Modifier = Modifier, index: Int = 1, share: Int? = null) {
    val locale = com.example.fitnesstracker.ui.components.appLocale()
    val format = remember(locale) { java.text.NumberFormat.getIntegerInstance(locale) }
    com.example.fitnesstracker.ui.theme.LeafSurface(modifier, color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp,
            bottomStart = if (index == 0) 28.dp else 8.dp,
            bottomEnd = if (index == 2) 28.dp else 8.dp)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(10.dp).background(color, androidx.compose.foundation.shape.CircleShape))
                Text(label, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(format.format(value.toDouble()), style = MaterialTheme.typography.titleLarge,
                    fontFamily = com.example.fitnesstracker.ui.theme.NumericFontFamily, color = MaterialTheme.colorScheme.onSurface)
                Text(stringResource(R.string.unit_g), Modifier.padding(bottom = 3.dp), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (share != null) Text("$share%", style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

@Composable
private fun MealSection(
    type: String,
    meals: List<Meal>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAdd: (Meal) -> Unit,
    onDelete: (Meal) -> Unit,
    onEdit: (Meal) -> Unit,
    onSaveTemplate: (Meal) -> Unit,
    products: List<com.example.fitnesstracker.data.entities.FoodProduct> = emptyList(),
    onCreateProduct: (com.example.fitnesstracker.data.entities.FoodProduct) -> Unit = {},
    onDeleteProduct: (com.example.fitnesstracker.data.entities.FoodProduct) -> Unit = {},
    onAddFromProduct: (com.example.fitnesstracker.data.entities.FoodProduct, Float) -> Unit = { _, _ -> }
) {
    val colors = MaterialTheme.colorScheme
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, spring(dampingRatio = .7f, stiffness = 400f), label = "arrow")
    val filled = meals.isNotEmpty()
    val kcal = meals.sumOf { it.calories.toDouble() }.toInt()
    val mealIcon = when (type) {
        stringResource(R.string.nutrition_breakfast) -> LeafIcons.Coffee
        stringResource(R.string.nutrition_lunch) -> LeafIcons.LunchDining
        stringResource(R.string.nutrition_snack) -> LeafIcons.Cookie
        stringResource(R.string.nutrition_dinner) -> LeafIcons.RamenDining
        else -> LeafIcons.Restaurant
    }
    com.example.fitnesstracker.ui.theme.LeafSurface(
        modifier = Modifier.fillMaxWidth().animateContentSize(spring(dampingRatio = .9f, stiffness = 500f)),
        shape = RoundedCornerShape(28.dp), color = colors.surfaceContainer) {
        Column {
            Row(Modifier.fillMaxWidth().bouncyClick { onToggle() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                com.example.fitnesstracker.ui.components.LeafIconBadge(mealIcon,
                    tone = if (filled) com.example.fitnesstracker.ui.components.LeafTone.Solid
                        else com.example.fitnesstracker.ui.components.LeafTone.Primary,
                    shape = if (filled) com.example.fitnesstracker.ui.components.LeafRosette(8) else null)
                Column(Modifier.weight(1f)) {
                    Text(type, style = MaterialTheme.typography.titleMedium)
                    Text(if (filled) stringResource(R.string.b15_meal_count, meals.size) else stringResource(R.string.nutrition_meal_empty),
                        style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
                if (filled) com.example.fitnesstracker.ui.theme.LeafSurface(shape = CircleShape, color = colors.primaryContainer,
                    contentColor = colors.onPrimaryContainer) {
                    Text("$kcal " + stringResource(R.string.unit_kcal), Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge, maxLines = 1)
                }
                Box(Modifier.size(36.dp).background(colors.surfaceContainerHighest, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(LeafIcons.ExpandMore, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(22.dp).rotate(arrowRotation))
                }
            }
            if (filled) {
                val parts = listOf(meals.sumOf { it.proteins.toDouble() }.toFloat() * 4f, meals.sumOf { it.fats.toDouble() }.toFloat() * 9f,
                    meals.sumOf { it.carbs.toDouble() }.toFloat() * 4f)
                val total = parts.sum()
                val tones = listOf(colors.primary, colors.secondary, colors.tertiary)
                if (total > 0f) Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 14.dp).height(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    parts.forEachIndexed { index, value ->
                        if (value > 0f) Box(Modifier.weight(value / total).fillMaxHeight().background(tones[index], RoundedCornerShape(4.dp)))
                    }
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    meals.forEachIndexed { index, meal ->
                        key(meal.id) {
                            MealRow(meal = meal, index = index, count = meals.size, onDelete = onDelete, onEdit = onEdit,
                                onSaveTemplate = onSaveTemplate)
                        }
                    }
                    if (filled) Spacer(Modifier.height(8.dp))
                    InlineAddMealForm(type = type, onAdd = onAdd, products = products, onCreateProduct = onCreateProduct,
                        onDeleteProduct = onDeleteProduct, onAddFromProduct = onAddFromProduct)
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun MealRow(meal: Meal, index: Int, count: Int, onDelete: (Meal) -> Unit, onEdit: (Meal) -> Unit, onSaveTemplate: (Meal) -> Unit) {
    val colors = MaterialTheme.colorScheme
    var showEdit by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    if (showEdit) {
        EditMealDialog(meal = meal, onSave = { onEdit(it); showEdit = false }, onDismiss = { showEdit = false })
    }
    com.example.fitnesstracker.ui.theme.LeafSurface(onClick = { showEdit = true }, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = if (index == 0) 20.dp else 6.dp, topEnd = if (index == 0) 20.dp else 6.dp,
            bottomStart = if (index == count - 1) 20.dp else 6.dp, bottomEnd = if (index == count - 1) 20.dp else 6.dp),
        color = colors.surfaceContainerHigh) {
        Row(Modifier.padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(meal.name, style = MaterialTheme.typography.titleSmall, maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MacroPill("${meal.proteins.toInt()}", stringResource(R.string.macro_p), colors.primary)
                    MacroPill("${meal.fats.toInt()}", stringResource(R.string.macro_f), colors.secondary)
                    MacroPill("${meal.carbs.toInt()}", stringResource(R.string.macro_c), colors.tertiary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${meal.calories.roundToInt()}", style = MaterialTheme.typography.titleLarge,
                    fontFamily = com.example.fitnesstracker.ui.theme.NumericFontFamily)
                Text(stringResource(R.string.unit_kcal), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
            Column {
                com.example.fitnesstracker.ui.components.LeafIconButton(onClick = { onSaveTemplate(meal); saved = true },
                    modifier = Modifier.size(40.dp)) {
                    Icon(if (saved) LeafIcons.Bookmark else LeafIcons.BookmarkBorder, stringResource(R.string.b7_save_template),
                        tint = colors.primary, modifier = Modifier.size(20.dp))
                }
                com.example.fitnesstracker.ui.components.LeafIconButton(onClick = { onDelete(meal) }, modifier = Modifier.size(40.dp)) {
                    Icon(LeafIcons.DeleteOutline, stringResource(R.string.common_delete), tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun EditMealDialog(meal: Meal, onSave: (Meal) -> Unit, onDismiss: () -> Unit) {
    var name     by remember { mutableStateOf(meal.name) }
    var calories by remember { mutableStateOf(meal.calories.roundToInt().toString()) }
    var proteins by remember { mutableStateOf(meal.proteins.toInt().toString()) }
    var fats     by remember { mutableStateOf(meal.fats.toInt().toString()) }
    var carbs    by remember { mutableStateOf(meal.carbs.toInt().toString()) }

    LeafEditSheet(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.common_edit)) },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(value = name, onValueChange = { name = it.take(40) },
                    label = stringResource(R.string.nutrition_dish_name), modifier = Modifier.fillMaxWidth())
                AppTextField(value = calories, onValueChange = { calories = it.filter { c -> c.isDigit() }.take(5) },
                    label = stringResource(R.string.nutrition_calories_kcal), modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Number)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(value = proteins, onValueChange = { proteins = it.filter { c -> c.isDigit() }.take(4) },
                        label = stringResource(R.string.nutrition_proteins), modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                    AppTextField(value = fats, onValueChange = { fats = it.filter { c -> c.isDigit() }.take(4) },
                        label = stringResource(R.string.nutrition_fats), modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                    AppTextField(value = carbs, onValueChange = { carbs = it.filter { c -> c.isDigit() }.take(4) },
                        label = stringResource(R.string.nutrition_carbs), modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                }
            }
        },
        confirmButton = {
            LeafButton(
                enabled = name.trim().isNotEmpty() && (calories.toIntOrNull() ?: 0) > 0,
                onClick = {
                    onSave(meal.copy(
                        name = name.trim(),
                        calories = calories.toFloatOrNull() ?: meal.calories,
                        proteins = proteins.toFloatOrNull() ?: 0f,
                        fats = fats.toFloatOrNull() ?: 0f,
                        carbs = carbs.toFloatOrNull() ?: 0f
                    ))
                },
                shape = RoundedCornerShape(50)
            ) { Text(stringResource(R.string.common_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    )
}

@Composable
private fun InlineAddMealForm(
    type: String,
    onAdd: (Meal) -> Unit,
    products: List<com.example.fitnesstracker.data.entities.FoodProduct> = emptyList(),
    onCreateProduct: (com.example.fitnesstracker.data.entities.FoodProduct) -> Unit = {},
    onDeleteProduct: (com.example.fitnesstracker.data.entities.FoodProduct) -> Unit = {},
    onAddFromProduct: (com.example.fitnesstracker.data.entities.FoodProduct, Float) -> Unit = { _, _ -> }
) {
    var showProducts by remember { mutableStateOf(false) }
    if (showProducts) {
        ProductsPickerDialog(
            products = products,
            onCreate = onCreateProduct,
            onDelete = onDeleteProduct,
            onPick = { p, grams -> onAddFromProduct(p, grams); showProducts = false },
            onDismiss = { showProducts = false }
        )
    }
    var name by rememberSaveable(type) { mutableStateOf("") }
    var calories by rememberSaveable(type) { mutableStateOf("") }
    var proteins by rememberSaveable(type) { mutableStateOf("") }
    var fats by rememberSaveable(type) { mutableStateOf("") }
    var carbs by rememberSaveable(type) { mutableStateOf("") }
    var manual by rememberSaveable(type) { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    fun reset() { name = ""; calories = ""; proteins = ""; fats = ""; carbs = "" }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            com.example.fitnesstracker.ui.theme.LeafSurface(onClick = { showProducts = true }, modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 8.dp, bottomEnd = 8.dp),
                color = colors.secondaryContainer, contentColor = colors.onSecondaryContainer) {
                Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(LeafIcons.Skillet, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                    com.example.fitnesstracker.ui.components.LeafFitText(stringResource(R.string.nutrition_from_product),
                        style = MaterialTheme.typography.labelLarge)
                }
            }
            com.example.fitnesstracker.ui.theme.LeafSurface(onClick = { manual = !manual }, modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 28.dp, bottomEnd = 28.dp),
                color = if (manual) colors.primary else colors.primaryContainer,
                contentColor = if (manual) colors.onPrimary else colors.onPrimaryContainer) {
                Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (manual) LeafIcons.Close else LeafIcons.Add, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                    com.example.fitnesstracker.ui.components.LeafFitText(stringResource(R.string.b15_meal_manual),
                        style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        AnimatedVisibility(visible = manual) {
            com.example.fitnesstracker.ui.theme.LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                color = colors.surfaceContainerHigh) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.nutrition_add_to, type), style = MaterialTheme.typography.titleSmall, maxLines = 2)
                    AppTextField(value = name, onValueChange = { name = it }, label = stringResource(R.string.nutrition_dish_name),
                        modifier = Modifier.fillMaxWidth())
                    StepperDecimalField(value = calories, onChange = { calories = it.filter { c -> c.isDigit() || c == '.' } },
                        label = stringResource(R.string.nutrition_calories_kcal), step = 10f, max = 5000f, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AppTextField(value = proteins, onValueChange = { proteins = it.filter { c -> c.isDigit() || c == '.' } },
                            label = stringResource(R.string.nutrition_proteins), keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f))
                        AppTextField(value = fats, onValueChange = { fats = it.filter { c -> c.isDigit() || c == '.' } },
                            label = stringResource(R.string.nutrition_fats), keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f))
                        AppTextField(value = carbs, onValueChange = { carbs = it.filter { c -> c.isDigit() || c == '.' } },
                            label = stringResource(R.string.nutrition_carbs), keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        com.example.fitnesstracker.ui.components.LeafFilledTonalIconButton(onClick = { reset() }, modifier = Modifier.size(52.dp)) {
                            Icon(LeafIcons.Restore, stringResource(R.string.common_clear))
                        }
                        LeafButton(enabled = name.isNotBlank(), onClick = {
                            if (name.isNotBlank()) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onAdd(Meal(name = name.trim(), mealType = type, calories = calories.toFloatOrNull() ?: 0f,
                                    proteins = proteins.toFloatOrNull() ?: 0f, fats = fats.toFloatOrNull() ?: 0f,
                                    carbs = carbs.toFloatOrNull() ?: 0f))
                                reset()
                                manual = false
                            }
                        }, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
                            Icon(LeafIcons.Check, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.common_add))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelectDialog(dates: List<Long>, selectedDay: Long, onSelect: (Long) -> Unit, onDismiss: () -> Unit) {
    com.example.fitnesstracker.ui.components.LeafDatePicker(selectedDay, onSelect, onDismiss)
}

@Composable
private fun MacroPill(value: String, label: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.12f)) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(value, style = MaterialTheme.typography.labelMedium, color = color,
                fontFamily = com.example.fitnesstracker.ui.theme.NumericFontFamily)
            Text(label, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.75f))
        }
    }
}

@Composable
private fun SavedMealsRow(
    saved: List<com.example.fitnesstracker.data.entities.SavedMeal>,
    onAdd: (com.example.fitnesstracker.data.entities.SavedMeal, Float) -> Unit,
    onRemove: (com.example.fitnesstracker.data.entities.SavedMeal) -> Unit
) {
    var portionFor by remember { mutableStateOf<com.example.fitnesstracker.data.entities.SavedMeal?>(null) }

    portionFor?.let { target ->
        LeafEditSheet(
            onDismissRequest = { portionFor = null },
            title = { Text(target.name) },
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.nutrition_portion_title),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0.5f, 1f, 1.5f, 2f).forEach { factor ->
                            Surface(
                                modifier = Modifier.weight(1f).height(44.dp).bouncyClick {
                                    onAdd(target, factor); portionFor = null
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = if (factor == 1f) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        if (factor % 1f == 0f) "×${factor.toInt()}" else "×$factor",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (factor == 1f) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { portionFor = null }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)) {
            com.example.fitnesstracker.ui.components.LeafIconBadge(LeafIcons.Bookmark, size = com.example.fitnesstracker.ui.components.LeafIconSize.Small, tone = com.example.fitnesstracker.ui.components.LeafTone.Secondary)
            Text(stringResource(R.string.nutrition_saved_meals), style = MaterialTheme.typography.titleSmall)
        }
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(saved.size) { idx ->
                val item = saved[idx]
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.bouncyClick { portionFor = item }
                ) {
                    Row(
                        Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column {
                            Text(item.name, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                            Text("${item.calories.roundToInt()} ${stringResource(R.string.unit_kcal)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center) {
                            Icon(com.example.fitnesstracker.ui.components.LeafIcons.Add, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                        }
                        Box(Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)).bouncyClick { onRemove(item) },
                            contentAlignment = Alignment.Center) {
                            Icon(com.example.fitnesstracker.ui.components.LeafIcons.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductsPickerDialog(
    products: List<com.example.fitnesstracker.data.entities.FoodProduct>,
    onCreate: (com.example.fitnesstracker.data.entities.FoodProduct) -> Unit,
    onDelete: (com.example.fitnesstracker.data.entities.FoodProduct) -> Unit,
    onPick: (com.example.fitnesstracker.data.entities.FoodProduct, Float) -> Unit,
    onDismiss: () -> Unit
) {
    var creating by remember { mutableStateOf(false) }
    var pName by remember { mutableStateOf("") }
    var pCal by remember { mutableStateOf("") }
    var pP by remember { mutableStateOf("") }
    var pF by remember { mutableStateOf("") }
    var pC by remember { mutableStateOf("") }
    var gramsFor by remember { mutableStateOf<com.example.fitnesstracker.data.entities.FoodProduct?>(null) }
    var grams by remember { mutableStateOf("100") }

    LeafEditSheet(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nutrition_products_title)) },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                gramsFor?.let { target ->
                    Text(target.name, style = MaterialTheme.typography.titleSmall)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppTextField(
                            value = grams,
                            onValueChange = { grams = it.filter { c -> c.isDigit() } },
                            label = stringResource(R.string.nutrition_grams),
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        val g = grams.toFloatOrNull()
                        Surface(
                            modifier = Modifier.size(48.dp).bouncyClick {
                                g?.takeIf { it in 1f..3000f }?.let { onPick(target, it) }
                            },
                            shape = CircleShape,
                            color = if (g != null && g in 1f..3000f) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(com.example.fitnesstracker.ui.components.LeafIcons.Check, null, modifier = Modifier.size(20.dp),
                                    tint = if (g != null && g in 1f..3000f) MaterialTheme.colorScheme.onPrimary
                                           else MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                    val g2 = grams.toFloatOrNull() ?: 0f
                    Text(
                        "${(target.caloriesPer100 * g2 / 100f).roundToInt()} ${stringResource(R.string.unit_kcal)} · " +
                        "${(target.proteinsPer100 * g2 / 100f).toInt()}${stringResource(R.string.macro_p)} " +
                        "${(target.fatsPer100 * g2 / 100f).toInt()}${stringResource(R.string.macro_f)} " +
                        "${(target.carbsPer100 * g2 / 100f).toInt()}${stringResource(R.string.macro_c)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { gramsFor = null }) { Text(stringResource(R.string.common_back)) }
                } ?: run {
                    if (creating) {
                        AppTextField(pName, { pName = it }, stringResource(R.string.nutrition_product_name), Modifier.fillMaxWidth())
                        AppTextField(pCal, { pCal = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.nutrition_kcal_per100), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppTextField(pP, { pP = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.macro_p), Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                            AppTextField(pF, { pF = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.macro_f), Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                            AppTextField(pC, { pC = it.filter { c -> c.isDigit() || c == '.' } }, stringResource(R.string.macro_c), Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { creating = false }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.common_cancel)) }
                            LeafButton(
                                onClick = {
                                    val cal = pCal.toFloatOrNull()
                                    if (pName.isNotBlank() && cal != null) {
                                        onCreate(com.example.fitnesstracker.data.entities.FoodProduct(
                                            name = pName.trim(), caloriesPer100 = cal,
                                            proteinsPer100 = pP.toFloatOrNull() ?: 0f,
                                            fatsPer100 = pF.toFloatOrNull() ?: 0f,
                                            carbsPer100 = pC.toFloatOrNull() ?: 0f
                                        ))
                                        pName = ""; pCal = ""; pP = ""; pF = ""; pC = ""; creating = false
                                    }
                                },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(50)
                            ) { Text(stringResource(R.string.common_save)) }
                        }
                    } else {
                        if (products.isEmpty()) {
                            Text(stringResource(R.string.nutrition_products_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        products.forEach { p ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().bouncyClick { gramsFor = p; grams = "100" },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(p.name, style = MaterialTheme.typography.bodyMedium)
                                        Text("${p.caloriesPer100.roundToInt()} ${stringResource(R.string.unit_kcal)} / 100 ${stringResource(R.string.unit_g)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Box(Modifier.size(26.dp).clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .bouncyClick { onDelete(p) },
                                        contentAlignment = Alignment.Center) {
                                        Icon(com.example.fitnesstracker.ui.components.LeafIcons.Close, null, modifier = Modifier.size(13.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        LeafTonalButton(
                            onClick = { creating = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(com.example.fitnesstracker.ui.components.LeafIcons.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.nutrition_new_product))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) }
        }
    )
}
