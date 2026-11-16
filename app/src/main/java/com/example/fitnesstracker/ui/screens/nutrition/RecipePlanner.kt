package com.example.fitnesstracker.ui.screens.nutrition

import com.example.fitnesstracker.ui.components.LeafIconButton as IconButton
import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.FoodProduct
import com.example.fitnesstracker.data.entities.Meal
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.*
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RecipePlannerCard(selectedDay: Long, mealTypes: List<String>, products: List<FoodProduct>,
    onRecord: (Meal, (Boolean) -> Unit) -> Unit) {
    val context = LocalContext.current
    val store = remember(context) { LifeStore.get(context) }
    val state by store.state.collectAsLeafState()
    var recordBusy by remember { mutableStateOf(false) }
    var recordError by remember { mutableStateOf(false) }
    var page by rememberSaveable { mutableStateOf("") }
    var selectedRecipeId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedRecipe = state.recipes.firstOrNull { it.id == selectedRecipeId }
    var planningDay by rememberSaveable(selectedDay) { mutableLongStateOf(selectedDay) }
    var choosingDay by rememberSaveable { mutableStateOf(false) }
    var servings by rememberSaveable { mutableStateOf("1") }
    var grams by rememberSaveable { mutableStateOf("250") }
    var type by rememberSaveable { mutableIntStateOf(0) }
    val daily = remember(state.meals, planningDay) { state.meals.filter { it.day == planningDay } }
    val colors = MaterialTheme.colorScheme
    LeafSurface(shape = RoundedCornerShape(28.dp), color = colors.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LeafCardHeader(LeafIcons.Skillet, stringResource(R.string.recipe_planner),
                subtitle = stringResource(R.string.recipe_planner_summary, state.recipes.size, daily.size), tone = LeafTone.Secondary)
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                PlannerTile(LeafIcons.MenuBook, stringResource(R.string.recipe_book), state.recipes.size, 0, LeafTone.Primary,
                    Modifier.weight(1f).fillMaxHeight()) { page = "recipes" }
                PlannerTile(LeafIcons.CalendarMonth, stringResource(R.string.meal_plan), daily.size, 1, LeafTone.Tertiary,
                    Modifier.weight(1f).fillMaxHeight()) { page = "plan" }
                PlannerTile(LeafIcons.Grocery, stringResource(R.string.shopping_list), state.shoppingItems.size, 2, LeafTone.Secondary,
                    Modifier.weight(1f).fillMaxHeight()) { page = "shopping" }
            }
        }
    }
    if (choosingDay) LeafDatePicker(planningDay, { planningDay = it; choosingDay = false }, { choosingDay = false })
    if (recordError) Text(stringResource(R.string.storage_failed), color = MaterialTheme.colorScheme.error)
    when (if (choosingDay) "" else page) {
        "recipes" -> LeafEditSheet(onDismissRequest = { page = "" },
            title = { Text(stringResource(R.string.recipe_book)) },
            content = {
                LeafButton(onClick = { selectedRecipeId = null; page = "editor" }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                    Icon(LeafIcons.Add, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.recipe_new))
                }
                if (state.recipes.isEmpty()) FeatureEmpty(stringResource(R.string.recipe_book),
                    stringResource(R.string.design_recipe_empty), LeafIcons.Restaurant)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    state.recipes.forEachIndexed { index, recipe ->
                        val portion = recipe.portion(100f)
                        LeafSurface(shape = groupedLeafShape(index, state.recipes.size), color = colors.surfaceContainer) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    LeafIconBadge(LeafIcons.Restaurant, size = LeafIconSize.Small, tone = LeafTone.Primary)
                                    Column(Modifier.weight(1f)) {
                                        Text(recipe.name, style = MaterialTheme.typography.titleMedium, maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                        Text(stringResource(R.string.recipe_yield, recipe.cookedGrams.toInt(), recipe.ingredients.size),
                                            style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                    }
                                }
                                PlannerMacros(portion[0], portion[1], portion[2], portion[3], stringResource(R.string.b15_per_100))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    LeafTonalButton(onClick = { selectedRecipeId = recipe.id; page = "portion" }, modifier = Modifier.weight(1f)) {
                                        Icon(LeafIcons.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                                        LeafFitText(stringResource(R.string.recipe_portion))
                                    }
                                    IconButton(onClick = { selectedRecipeId = recipe.id; page = "editor" }) {
                                        Icon(LeafIcons.Edit, stringResource(R.string.common_edit))
                                    }
                                    IconButton(onClick = { store.update { it.copy(
                                        recipes = it.recipes.filterNot { r -> r.id == recipe.id },
                                        meals = it.meals.filterNot { m -> m.recipeId == recipe.id }
                                    ) } }) {
                                        Icon(LeafIcons.DeleteOutline, stringResource(R.string.common_delete), tint = colors.error)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { page = "" }) { Text(stringResource(R.string.common_close)) } })
        "editor" -> RecipeEditor(selectedRecipe, products, onSave = { recipe ->
            store.update { old -> old.copy(recipes = old.recipes.filterNot { it.id == recipe.id } + recipe) }
            selectedRecipeId = null
            page = "recipes"
        }, onDismiss = { selectedRecipeId = null; page = "recipes" })
        "portion" -> selectedRecipe?.let { recipe ->
            val portionGrams = grams.replace(',', '.').toFloatOrNull()
            val count = servings.toIntOrNull()
            val totalGrams = (portionGrams ?: 0f) * (count ?: 0)
            val validPortion = portionGrams != null && portionGrams in 1f..10000f && count != null && count in 1..100
            LeafEditSheet(onDismissRequest = { page = "recipes" }, title = { Text(recipe.name) },
                content = {
                    AppTextField(grams, { grams = it }, stringResource(R.string.recipe_portion_grams),
                        Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
                    AppTextField(servings, { servings = it.filter(Char::isDigit).take(3) },
                        stringResource(R.string.recipe_servings), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number)
                    PlannerDateRow(DateUtils.formatDate(planningDay)) { choosingDay = true }
                    ExpressiveChoiceRow(mealTypes, emptyList(), type.coerceIn(mealTypes.indices), { type = it })
                    val amount = totalGrams.takeIf { validPortion }
                    if (amount != null && amount > 0) {
                        val macros = recipe.portion(amount)
                        PlannerMacros(macros[0], macros[1], macros[2], macros[3],
                            stringResource(R.string.recipe_total_grams, totalGrams.toInt()))
                    }
                }, confirmButton = {
                    LeafButton(enabled = validPortion && !recordBusy,
                        onClick = {
                            val amount = totalGrams
                            val macros = recipe.portion(amount)
                            recordBusy = true; recordError = false
                            onRecord(Meal(date = planningDay, mealType = mealTypes[type.coerceIn(mealTypes.indices)],
                                name = recipe.name, calories = macros[0], proteins = macros[1],
                                fats = macros[2], carbs = macros[3])) { saved ->
                                recordBusy = false; recordError = !saved
                                if (saved) page = ""
                            }
                        }) { Text(stringResource(R.string.recipe_record)) }
                }, dismissButton = { TextButton(enabled = validPortion && !recordBusy,
                    onClick = {
                        val amount = totalGrams
                        store.update { it.copy(meals = it.meals + PlannedMeal(day = planningDay, recipeId = recipe.id, grams = amount)) }
                        page = ""
                    }) { Text(stringResource(R.string.meal_plan_add)) } })
        }
        "plan" -> LeafEditSheet(onDismissRequest = { page = "" },
            title = { Text(stringResource(R.string.meal_plan)) },
            content = {
                PlannerDateRow(DateUtils.formatDate(planningDay)) { choosingDay = true }
                if (state.recipes.isNotEmpty()) FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.recipes.forEach { recipe ->
                        LeafSurface(onClick = { selectedRecipeId = recipe.id; page = "portion" }, shape = RoundedCornerShape(16.dp),
                            color = colors.secondaryContainer, contentColor = colors.onSecondaryContainer) {
                            Row(Modifier.heightIn(min = 44.dp).padding(start = 10.dp, end = 14.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(LeafIcons.Add, null, Modifier.size(18.dp))
                                Text(recipe.name, style = MaterialTheme.typography.labelLarge, maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                if (daily.isEmpty()) FeatureEmpty(stringResource(R.string.meal_plan),
                    stringResource(R.string.design_plan_empty), LeafIcons.CalendarMonth)
                val planned = daily.filter { row -> state.recipes.any { it.id == row.recipeId } }
                if (planned.isNotEmpty()) LeafWavyProgress(planned.count { it.prepared }.toFloat() / planned.size, false,
                    Modifier.fillMaxWidth().height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    planned.forEachIndexed { index, row ->
                        val recipe = state.recipes.first { it.id == row.recipeId }
                        val macros = recipe.portion(row.grams)
                        LeafSurface(shape = groupedLeafShape(index, planned.size),
                            color = if (row.prepared) colors.primaryContainer else colors.surfaceContainer) {
                            Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = row.prepared, onCheckedChange = { checked ->
                                    store.update { it.copy(meals = it.meals.map { item ->
                                        if (item.id == row.id) item.copy(prepared = checked) else item
                                    }) }
                                })
                                Column(Modifier.weight(1f)) {
                                    Text(recipe.name, style = MaterialTheme.typography.titleSmall, maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                    Text(row.grams.toInt().toString() + " " + stringResource(R.string.unit_g) + " · " + macros[0].toInt() + " " +
                                        stringResource(R.string.unit_kcal) + " · " + stringResource(if (row.prepared) R.string.meal_prepared
                                            else R.string.meal_prepare), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                }
                                LeafFilledTonalIconButton(enabled = !recordBusy, onClick = {
                                    recordBusy = true; recordError = false
                                    onRecord(Meal(date = row.day, mealType = mealTypes.first(), name = recipe.name,
                                        calories = macros[0], proteins = macros[1], fats = macros[2], carbs = macros[3])) { saved ->
                                        recordBusy = false; recordError = !saved
                                        if (saved) store.update { it.copy(meals = it.meals.filterNot { item -> item.id == row.id }) }
                                    }
                                }) { Icon(LeafIcons.Check, stringResource(R.string.recipe_record)) }
                                IconButton(onClick = { store.update { it.copy(meals = it.meals.filterNot { item -> item.id == row.id }) } }) {
                                    Icon(LeafIcons.DeleteOutline, stringResource(R.string.common_delete))
                                }
                            }
                        }
                    }
                }
            }, confirmButton = { TextButton(onClick = { page = "" }) { Text(stringResource(R.string.common_close)) } })
        "shopping" -> ShoppingListSheet(store) { page = "" }
    }
}

@Composable
private fun RecipeEditor(current: LeafRecipe?, products: List<FoodProduct>,
    onSave: (LeafRecipe) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable(current?.id) { mutableStateOf(current?.name.orEmpty()) }
    var cooked by rememberSaveable(current?.id) { mutableStateOf(current?.cookedGrams?.toString().orEmpty()) }
    var ingredients by rememberSaveable(current?.id, stateSaver = androidx.compose.runtime.saveable.listSaver<List<RecipeIngredient>, Any>(
        save = { list -> list.flatMap { listOf(it.name, it.grams, it.calories100, it.proteins100, it.fats100, it.carbs100) } },
        restore = { values -> values.chunked(6).map { RecipeIngredient(it[0] as String, it[1] as Float,
            it[2] as Float, it[3] as Float, it[4] as Float, it[5] as Float) } }
    )) { mutableStateOf(current?.ingredients.orEmpty()) }
    var ingredientName by rememberSaveable(current?.id) { mutableStateOf("") }
    var ingredientGrams by rememberSaveable(current?.id) { mutableStateOf("") }
    var calories by rememberSaveable(current?.id) { mutableStateOf("") }
    var proteins by rememberSaveable(current?.id) { mutableStateOf("") }
    var fats by rememberSaveable(current?.id) { mutableStateOf("") }
    var carbs by rememberSaveable(current?.id) { mutableStateOf("") }
    var productQuery by rememberSaveable { mutableStateOf("") }
    var adding by rememberSaveable(current?.id) { mutableStateOf(current == null) }
    fun numeric(input: String) = input.replace(',', '.').toFloatOrNull()
    LeafEditSheet(onDismissRequest = onDismiss,
        title = { Text(stringResource(if (current == null) R.string.recipe_new else R.string.common_edit)) },
        content = {
            FeatureSection(stringResource(R.string.design_recipe_basics), subtitle = stringResource(R.string.design_recipe_weight_hint)) {
                AppTextField(name, { name = it.take(120) }, stringResource(R.string.recipe_name), Modifier.fillMaxWidth())
                AppTextField(cooked, { cooked = it }, stringResource(R.string.recipe_cooked_weight), Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Decimal)
            }
            FeatureSection(stringResource(R.string.design_ingredients, ingredients.size)) {
                if (ingredients.isEmpty()) Text(stringResource(R.string.design_ingredients_hint),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ingredients.forEachIndexed { index, ingredient ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text((index + 1).toString(), Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(ingredient.name, style = MaterialTheme.typography.titleSmall)
                            Text(ingredient.grams.toInt().toString() + " " + stringResource(R.string.unit_g),
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { ingredients = ingredients.filterIndexed { n, _ -> n != index } }) {
                            Icon(LeafIcons.Close, stringResource(R.string.common_delete))
                        }
                    }
                }
                if (!adding) LeafTonalButton(onClick = { adding = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(LeafIcons.Add, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.recipe_add_ingredient))
                }
            }
            if (adding) FeatureSection(stringResource(R.string.recipe_add_ingredient), subtitle = stringResource(R.string.design_ingredient_hint)) {
                AppSearchBar(productQuery, { productQuery = it }, stringResource(R.string.recipe_pick_product), Modifier.fillMaxWidth())
                if (productQuery.isNotBlank()) {
                    val matches = products.filter { it.name.contains(productQuery, ignoreCase = true) }.take(5)
                    if (matches.isEmpty()) Text(stringResource(R.string.design_product_missing), style = MaterialTheme.typography.bodySmall)
                    matches.forEach { product ->
                        LeafSurface(onClick = {
                            ingredientName = product.name
                            calories = product.caloriesPer100.toString(); proteins = product.proteinsPer100.toString()
                            fats = product.fatsPer100.toString(); carbs = product.carbsPer100.toString()
                            productQuery = ""
                        }, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                            Text(product.name, Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                AppTextField(ingredientName, { ingredientName = it.take(120) }, stringResource(R.string.recipe_ingredient), Modifier.fillMaxWidth())
                AppTextField(ingredientGrams, { ingredientGrams = it }, stringResource(R.string.recipe_ingredient_grams),
                    Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
                Text(stringResource(R.string.design_per_100), style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
                AppTextField(calories, { calories = it }, stringResource(R.string.recipe_calories_100),
                    Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
                AppTextField(proteins, { proteins = it }, stringResource(R.string.macro_p), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
                AppTextField(fats, { fats = it }, stringResource(R.string.macro_f), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
                AppTextField(carbs, { carbs = it }, stringResource(R.string.macro_c), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
                LeafTonalButton(modifier = Modifier.fillMaxWidth(), enabled = ingredientName.isNotBlank() &&
                    (numeric(ingredientGrams) ?: 0f) in 1f..100000f &&
                    listOf(calories, proteins, fats, carbs).all { (numeric(it) ?: -1f) in 0f..10000f }, onClick = {
                    ingredients = ingredients + RecipeIngredient(ingredientName.trim(), numeric(ingredientGrams)!!,
                        numeric(calories)!!, numeric(proteins)!!, numeric(fats)!!, numeric(carbs)!!)
                    ingredientName = ""; ingredientGrams = ""; calories = ""; proteins = ""; fats = ""; carbs = ""
                    adding = false
                }) { Text(stringResource(R.string.recipe_add_ingredient)) }
            }
            if (ingredients.isNotEmpty() && (numeric(cooked) ?: 0f) > 0f) {
                val macros = LeafRecipe(name = name, cookedGrams = numeric(cooked)!!, ingredients = ingredients).portion(100f)
                FeatureSection(stringResource(R.string.design_recipe_preview)) {
                    Text(stringResource(R.string.recipe_per_100, macros[0].toInt(), macros[1].toInt(),
                        macros[2].toInt(), macros[3].toInt()), style = MaterialTheme.typography.titleMedium)
                }
            }
        },
        confirmButton = { LeafButton(enabled = name.isNotBlank() &&
            (numeric(cooked) ?: 0f) in 1f..100000f && ingredients.isNotEmpty(),
            onClick = { onSave(LeafRecipe(current?.id ?: java.util.UUID.randomUUID().toString(),
                name.trim(), numeric(cooked)!!, ingredients)) }) { Text(stringResource(R.string.common_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } })
}

@Composable
private fun PlannerTile(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, count: Int, index: Int, tone: LeafTone,
    modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    LeafSurface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(
        topStart = if (index == 0) 24.dp else 8.dp, bottomStart = if (index == 0) 24.dp else 8.dp,
        topEnd = if (index == 2) 24.dp else 8.dp, bottomEnd = if (index == 2) 24.dp else 8.dp), color = colors.surfaceContainerHigh) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LeafIconBadge(icon, size = LeafIconSize.Small, tone = tone)
            Text(count.toString(), style = MaterialTheme.typography.headlineSmall)
            LeafFitText(label, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlannerDateRow(date: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    LeafSurface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = colors.tertiaryContainer,
        contentColor = colors.onTertiaryContainer) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(LeafIcons.filled(LeafIcons.CalendarMonth), null, Modifier.size(22.dp))
            Text(date, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Icon(LeafIcons.ChevronRight, null, Modifier.size(20.dp))
        }
    }
}

@Composable
private fun PlannerMacros(kcal: Float, proteins: Float, fats: Float, carbs: Float, caption: String) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(caption, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            listOf(kcal.toInt().toString() to stringResource(R.string.unit_kcal), proteins.toInt().toString() to stringResource(R.string.macro_p),
                fats.toInt().toString() to stringResource(R.string.macro_f), carbs.toInt().toString() to stringResource(R.string.macro_c))
                .forEachIndexed { index, (value, label) ->
                    LeafSurface(Modifier.weight(1f), shape = RoundedCornerShape(
                        topStart = if (index == 0) 16.dp else 6.dp, bottomStart = if (index == 0) 16.dp else 6.dp,
                        topEnd = if (index == 3) 16.dp else 6.dp, bottomEnd = if (index == 3) 16.dp else 6.dp),
                        color = if (index == 0) colors.primaryContainer else colors.surfaceContainerHigh) {
                        Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, style = MaterialTheme.typography.titleMedium)
                            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1)
                        }
                    }
                }
        }
    }
}
