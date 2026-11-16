package com.example.fitnesstracker.ui.screens.nutrition

import com.example.fitnesstracker.ui.components.LeafIconButton as IconButton
import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton
import com.example.fitnesstracker.ui.components.LeafCheckbox as Checkbox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.*
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ShoppingListSheet(store: LifeStore, onClose: () -> Unit) {
    val state by store.state.collectAsLeafState()
    var editor by rememberSaveable { mutableStateOf(false) }
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var bulk by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    val today = DateUtils.todayStart()
    val end = java.time.LocalDate.now().plusDays(7).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val generated = remember(state.meals, state.recipes, today) {
        state.meals.filter { !it.prepared && it.day in today until end }.flatMap { meal ->
            state.recipes.firstOrNull { it.id == meal.recipeId }?.let { recipe ->
                recipe.ingredients.map { ingredient -> ingredient.name.trim() to ingredient.grams * meal.grams / recipe.cookedGrams.coerceAtLeast(1f) }
            }.orEmpty()
        }.groupBy { it.first.lowercase(Locale.ROOT) }.mapValues { (_, rows) -> rows.first().first to rows.sumOf { it.second.toDouble() }.toInt() }
    }
    if (editor) LeafEditSheet(onDismissRequest = { editor = false }, title = { Text(stringResource(if (bulk) R.string.b5_shopping_bulk else R.string.b5_shopping_add)) }, content = {
        AppTextField(name, { name = it.take(if (bulk) 6000 else 120) }, stringResource(if (bulk) R.string.b5_shopping_lines else R.string.recipe_ingredient),
            Modifier.fillMaxWidth(), singleLine = !bulk, minLines = if (bulk) 5 else 1, maxLines = if (bulk) 12 else 1)
        if (!bulk) AppTextField(amount, { amount = it.take(60) }, stringResource(R.string.b5_shopping_amount), Modifier.fillMaxWidth())
    }, confirmButton = { LeafButton(enabled = name.isNotBlank(), onClick = {
        store.update { old ->
            if (bulk) old.copy(shoppingItems = old.shoppingItems + name.lines().map(String::trim).filter(String::isNotBlank).take(100).map { ShoppingItem(name = it.take(120)) })
            else {
                val previous = old.shoppingItems.firstOrNull { it.id == editing }
                val row = ShoppingItem(id = previous?.id ?: java.util.UUID.randomUUID().toString(), name = name.trim(), amount = amount.trim(), checked = previous?.checked ?: false)
                old.copy(shoppingItems = old.shoppingItems.filterNot { it.id == row.id } + row)
            }
        }
        editor = false; name = ""; amount = ""; editing = null
    }) { Text(stringResource(R.string.common_save)) } }, dismissButton = { TextButton(onClick = { editor = false }) { Text(stringResource(R.string.common_cancel)) } })
    else LeafEditSheet(onDismissRequest = onClose, title = { Text(stringResource(R.string.shopping_list)) }, content = {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LeafTonalButton(onClick = { editing = null; name = ""; amount = ""; bulk = false; editor = true }) { Text(stringResource(R.string.b5_shopping_add)) }
            TextButton(onClick = { editing = null; name = ""; amount = ""; bulk = true; editor = true }) { Text(stringResource(R.string.b5_shopping_bulk)) }
        }
        if (state.shoppingItems.isEmpty() && generated.isEmpty()) FeatureEmpty(stringResource(R.string.shopping_list), stringResource(R.string.b5_shopping_empty), LeafIcons.Restaurant)
        if (state.shoppingItems.isNotEmpty()) FeatureSection(stringResource(R.string.b5_shopping_manual)) {
            state.shoppingItems.sortedBy { it.checked }.forEach { item ->
                LeafSurface(shape = RoundedCornerShape(18.dp), color = if (item.checked) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Row(Modifier.fillMaxWidth().padding(end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(item.checked, { checked -> store.update { old -> old.copy(shoppingItems = old.shoppingItems.map { if (it.id == item.id) it.copy(checked = checked) else it }) } })
                        Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
                            Text(item.name, style = MaterialTheme.typography.bodyLarge, textDecoration = if (item.checked) TextDecoration.LineThrough else null)
                            if (item.amount.isNotBlank()) Text(item.amount, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { editing = item.id; name = item.name; amount = item.amount; bulk = false; editor = true }) { Icon(LeafIcons.Edit, stringResource(R.string.common_edit), Modifier.size(18.dp)) }
                        IconButton(onClick = { store.update { old -> old.copy(shoppingItems = old.shoppingItems.filterNot { it.id == item.id }) } }) { Icon(LeafIcons.Close, stringResource(R.string.common_delete), Modifier.size(18.dp)) }
                    }
                }
            }
            if (state.shoppingItems.any { it.checked }) TextButton(onClick = { store.update { it.copy(shoppingItems = it.shoppingItems.filterNot { row -> row.checked }) } }) {
                Text(stringResource(R.string.b5_shopping_clear))
            }
        }
        if (generated.isNotEmpty()) FeatureSection(stringResource(R.string.b5_shopping_recipes), subtitle = stringResource(R.string.shopping_week)) {
            generated.entries.sortedBy { it.key in state.shoppingChecked }.forEach { (key, value) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(key in state.shoppingChecked, { checked -> store.update { it.copy(shoppingChecked = if (checked) it.shoppingChecked + key else it.shoppingChecked - key) } })
                    Text(value.first + " · " + value.second + " " + stringResource(R.string.unit_g), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (key in state.shoppingChecked) TextDecoration.LineThrough else null)
                }
            }
            TextButton(onClick = { store.update { it.copy(shoppingChecked = emptySet()) } }) { Text(stringResource(R.string.shopping_reset)) }
        }
    }, confirmButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.common_close)) } })
}
