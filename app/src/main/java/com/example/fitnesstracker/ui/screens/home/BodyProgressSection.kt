package com.example.fitnesstracker.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.WeightRecord
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.LifeStore

@Composable
internal fun BodyProgressSection(store: LifeStore, history: List<WeightRecord>, onAdd: (Float) -> Unit,
    onUpdate: (WeightRecord, (Boolean) -> Unit) -> Unit,
    onDelete: (WeightRecord, (Boolean) -> Unit) -> Unit) {
    var measures by rememberSaveable { mutableStateOf(false) }
    val holder = rememberSaveableStateHolder()
    LeafSurface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column {
            Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.b6_body), style = MaterialTheme.typography.titleMedium)
                LeafViewSelector(measures, stringResource(R.string.b6_weight), stringResource(R.string.b6_measures)) { measures = it }
            }
            holder.SaveableStateProvider(if (measures) "measurements" else "weight") {
                if (measures) ProgressStoryCard(store, embedded = true)
                else WeightTrackingCard(history, onAdd, onUpdate, onDelete, embedded = true)
            }
        }
    }
}
