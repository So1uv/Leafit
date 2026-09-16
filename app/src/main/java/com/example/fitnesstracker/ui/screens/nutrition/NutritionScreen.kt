package com.example.fitnesstracker.ui.screens.nutrition

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
    val meals        by vm.meals.collectAsState()
    val selectedDay  by vm.selectedDay.collectAsState()
    val goal         by vm.dailyCalorieGoal.collectAsState()
    val totalCal     by vm.totalCalories.collectAsState()
    val proteins     by vm.totalProteins.collectAsState()
    val fats         by vm.totalFats.collectAsState()
    val carbs        by vm.totalCarbs.collectAsState()
    val dates        by vm.availableDates.collectAsState()
    val waterMl      by vm.waterMlToday.collectAsState()
    val waterGoal    by vm.waterGoalMl.collectAsState()
    val savedMeals   by vm.savedMeals.collectAsState()
    val products     by vm.products.collectAsState()
    val context      = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    var expandedType   by remember { mutableStateOf<String?>(null) }

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



            item {
                NutritionSummaryCard(
                    totalCal = totalCal,
                    goal = goal,
                    proteins = proteins,
                    fats = fats,
                    carbs = carbs
                )
            }

            item {
                com.example.fitnesstracker.ui.components.HydrationCard(
                    waterMl = waterMl,
                    goalMl = waterGoal,
                    onAdd = { vm.addWater(it) },
                    onRemove = { vm.removeWater() }
                )
            }

            if (savedMeals.isNotEmpty()) {
                item {
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

            item {
                Text(stringResource(R.string.nutrition_meals_section), style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp))
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


        }
    }
}

@Composable
private fun NutritionSummaryCard(totalCal: Float, goal: Float, proteins: Float, fats: Float, carbs: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(Modifier.size(40.dp), Alignment.Center) {
                        Icon(LeafIcons.Restaurant, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
                    }
                }
                Text(stringResource(R.string.nutrition_calories_label), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RollingNumber(totalCal.roundToInt(), style = MaterialTheme.typography.headlineLarge)
                Text("/ ${goal.roundToInt()} " + stringResource(R.string.unit_kcal),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp))
            }
            Spacer(Modifier.height(14.dp))
            val budgetFrac = (totalCal / goal.coerceAtLeast(1f)).coerceIn(0f, 1.2f)
            val heroColor = MaterialTheme.colorScheme.primary
            GlowProgressBar(
                progress = budgetFrac.coerceIn(0f, 1f),
                color = heroColor,
                height = 8.dp
            )
            Spacer(Modifier.height(8.dp))
            if (totalCal < goal) {
                Row(verticalAlignment = Alignment.Bottom) {
                    RollingNumber(
                        (goal - totalCal).roundToInt(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = heroColor,
                        weight = FontWeight.Medium
                    )
                    Text(
                        " " + stringResource(R.string.nutrition_remaining_suffix),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    stringResource(R.string.nutrition_goal_reached),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Normal,
                    color = heroColor
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroItem(stringResource(R.string.nutrition_proteins), proteins, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                MacroItem(stringResource(R.string.nutrition_fats), fats, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                MacroItem(stringResource(R.string.nutrition_carbs), carbs, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MacroItem(label: String, value: Float, color: Color, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        MetricValueWithSuffix(
            value = "%.0f".format(value),
            suffix = " " + stringResource(R.string.unit_g),
            valueStyle = MaterialTheme.typography.titleMedium,
            suffixStyle = MaterialTheme.typography.labelSmall,
            color = color
        )
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val haptic = LocalHapticFeedback.current
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, spring(dampingRatio = .85f, stiffness = 500f), label = "arrow")

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(spring(dampingRatio = .9f, stiffness = 500f)),
        shape = RoundedCornerShape(if (expanded) 28.dp else 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            val (mealIcon, mealTint) = when (type) {
                stringResource(R.string.nutrition_breakfast) -> LeafIcons.WbSunny to MaterialTheme.colorScheme.tertiary
                stringResource(R.string.nutrition_lunch)     -> LeafIcons.LunchDining to MaterialTheme.colorScheme.primary
                stringResource(R.string.nutrition_snack)  -> LeafIcons.BakeryDining to MaterialTheme.colorScheme.secondary
                stringResource(R.string.nutrition_dinner)  -> LeafIcons.DinnerDining to MaterialTheme.colorScheme.secondary
                else       -> LeafIcons.Restaurant to MaterialTheme.colorScheme.primary
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(if (expanded) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer)
                    .bouncyClick { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(15.dp))
                        .background(mealTint.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(mealIcon, null, tint = mealTint, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(type, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text(
                        if (meals.isNotEmpty()) stringResource(R.string.nutrition_meal_summary, meals.sumOf { it.calories.toDouble() }.toInt(), meals.size) else stringResource(R.string.nutrition_meal_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (meals.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = mealTint.copy(alpha = 0.14f),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            "${meals.sumOf { it.calories.toDouble() }.toInt()}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Normal,
                            color = mealTint,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Icon(
                    com.example.fitnesstracker.ui.components.LeafIcons.ExpandMore,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(arrowRotation)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    if (meals.isEmpty()) {
                        Text(
                            stringResource(R.string.nutrition_no_records),
                            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        meals.forEach { meal ->
                            key(meal.id) {
                                var visible by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) { visible = true }
                                AnimatedVisibility(
                                    visible = visible,
                                    enter = androidx.compose.animation.fadeIn(tween(280)) +
                                            androidx.compose.animation.slideInVertically(
                                                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                            ) { it / 2 }
                                ) {
                                    MealRow(meal = meal, onDelete = onDelete, onEdit = onEdit, onSaveTemplate = onSaveTemplate)
                                }
                            }
                        }
                    }
                    InlineAddMealForm(type = type, onAdd = onAdd, products = products, onCreateProduct = onCreateProduct, onDeleteProduct = onDeleteProduct, onAddFromProduct = onAddFromProduct)
                }
            }
        }
    }
}

@Composable
private fun MealRow(meal: Meal, onDelete: (Meal) -> Unit, onEdit: (Meal) -> Unit, onSaveTemplate: (Meal) -> Unit) {
    var showEdit by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    if (showEdit) {
        EditMealDialog(meal = meal, onSave = { onEdit(it); showEdit = false }, onDismiss = { showEdit = false })
    }
    Row(
        Modifier.fillMaxWidth().bouncyClick { showEdit = true }.padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(meal.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Normal)
            Spacer(Modifier.height(3.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                MacroPill("${meal.calories.roundToInt()}", stringResource(R.string.unit_kcal), MaterialTheme.colorScheme.secondary)
                MacroPill("${meal.proteins.toInt()}", stringResource(R.string.macro_p), MaterialTheme.colorScheme.primary)
                MacroPill("${meal.fats.toInt()}", stringResource(R.string.macro_f), MaterialTheme.colorScheme.error)
                MacroPill("${meal.carbs.toInt()}", stringResource(R.string.macro_c), MaterialTheme.colorScheme.tertiary)
            }
        }
        Box(Modifier.size(30.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)).bouncyClick {
                onSaveTemplate(meal); saved = true
            },
            contentAlignment = Alignment.Center) {
            Icon(if (saved) LeafIcons.Bookmark else LeafIcons.BookmarkBorder, null,
                tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(15.dp))
        }
        Box(Modifier.size(30.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)).bouncyClick { showEdit = true },
            contentAlignment = Alignment.Center) {
            Icon(com.example.fitnesstracker.ui.components.LeafIcons.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
        }
        Box(Modifier.size(30.dp).clip(CircleShape).background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)).bouncyClick { onDelete(meal) },
            contentAlignment = Alignment.Center) {
            Icon(com.example.fitnesstracker.ui.components.LeafIcons.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
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
                    Icon(com.example.fitnesstracker.ui.components.LeafIcons.Add, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.nutrition_add_to, type), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                    modifier = Modifier.bouncyClick { showProducts = true }
                ) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(LeafIcons.Kitchen, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                        Text(stringResource(R.string.nutrition_from_product),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.nutrition_dish_name),
                modifier = Modifier.fillMaxWidth()
            )
            StepperDecimalField(
                value = calories,
                onChange = { calories = it.filter { c -> c.isDigit() || c == '.' } },
                label = stringResource(R.string.nutrition_calories_kcal),
                step = 10f,
                max = 5000f,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = proteins,
                    onValueChange = { proteins = it.filter { c -> c.isDigit() || c == '.' } },
                    label = stringResource(R.string.nutrition_proteins),
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    value = fats,
                    onValueChange = { fats = it.filter { c -> c.isDigit() || c == '.' } },
                    label = stringResource(R.string.nutrition_fats),
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    value = carbs,
                    onValueChange = { carbs = it.filter { c -> c.isDigit() || c == '.' } },
                    label = stringResource(R.string.nutrition_carbs),
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LeafButton(
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
                    Text(stringResource(R.string.common_add))
                }
                LeafTonalButton(
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
                    Text(stringResource(R.string.common_clear))
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
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.14f)) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Normal, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.75f), fontWeight = FontWeight.Medium)
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
                                        fontWeight = FontWeight.Medium,
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
            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                Box(Modifier.size(28.dp), Alignment.Center) {
                    Icon(LeafIcons.Bookmark, null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(16.dp))
                }
            }
            Text(stringResource(R.string.nutrition_saved_meals), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
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
                            Text(item.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
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
                    Text(target.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
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
                                        Text(p.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Normal)
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
