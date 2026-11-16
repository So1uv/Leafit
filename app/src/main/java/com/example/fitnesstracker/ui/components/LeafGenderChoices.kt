package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R

@Composable
fun LeafGenderChoices(selected: String, onSelect: (String) -> Unit, options: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExpressiveChoiceRow(options.map { it.second },
            options.map { (key, _) -> when (key) { "male" -> LeafIcons.Male; "female" -> LeafIcons.Female; else -> LeafIcons.Eco } },
            options.indexOfFirst { it.first == selected }.coerceAtLeast(0), { index -> onSelect(options[index].first) })
        if (selected == "other") Text(stringResource(R.string.profile_leaf_explanation), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
