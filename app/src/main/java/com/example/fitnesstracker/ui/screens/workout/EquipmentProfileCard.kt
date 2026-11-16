package com.example.fitnesstracker.ui.screens.workout

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
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.EquipmentProfile
import com.example.fitnesstracker.utils.LifeStore
import com.example.fitnesstracker.workout.ExerciseLibrary

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EquipmentProfileCard() {
    val context = LocalContext.current
    val store = remember(context) { LifeStore.get(context) }
    val life by store.state.collectAsLeafState()
    var open by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<String>()) }
    val active = life.equipmentProfiles.firstOrNull { it.id == life.activeEquipmentId }
    LeafSurface(onClick = { open = true }, shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            FeatureBadge(LeafIcons.FitnessCenter)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.equipment_profiles), style = MaterialTheme.typography.titleMedium)
                Text(active?.name ?: stringResource(R.string.library_all_equipment),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(LeafIcons.ChevronRight, null)
        }
    }
    if (open && !creating) LeafEditSheet(onDismissRequest = { open = false },
        title = { Text(stringResource(R.string.equipment_profiles)) },
        content = {
            Text(stringResource(R.string.design_equipment_hint), style = MaterialTheme.typography.bodyLarge)
            FilterChip(selected = active == null, onClick = { store.update { it.copy(activeEquipmentId = null) } },
                label = { Text(stringResource(R.string.library_all_equipment)) })
            life.equipmentProfiles.forEach { profile ->
                LeafSurface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(active?.id == profile.id,
                            onClick = { store.update { it.copy(activeEquipmentId = profile.id) } })
                        Column(Modifier.weight(1f).padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(profile.name, style = MaterialTheme.typography.titleMedium)
                            Text(profile.equipment.joinToString(" · ") { context.getString(ExerciseLibrary.tag(it)) },
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { editingId = profile.id; title = profile.name; selected = profile.equipment; creating = true }) {
                            Icon(LeafIcons.Edit, stringResource(R.string.common_edit))
                        }
                        IconButton(onClick = { store.update {
                            it.copy(equipmentProfiles = it.equipmentProfiles.filterNot { p -> p.id == profile.id },
                                activeEquipmentId = if (it.activeEquipmentId == profile.id) null else it.activeEquipmentId)
                        } }) { Icon(LeafIcons.DeleteOutline, stringResource(R.string.common_delete)) }
                    }
                }
            }
            LeafTonalButton(onClick = { editingId = null; title = ""; selected = emptySet(); creating = true },
                modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.equipment_new)) }
        }, confirmButton = { TextButton(onClick = { open = false }) { Text(stringResource(R.string.common_close)) } })
    if (creating) LeafEditSheet(onDismissRequest = { creating = false },
        title = { Text(stringResource(R.string.equipment_new)) },
        content = {
            AppTextField(title, { title = it.take(80) }, stringResource(R.string.equipment_name),
                Modifier.fillMaxWidth())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExerciseLibrary.equipment.forEach { tag ->
                    FilterChip(selected = tag in selected,
                        onClick = { selected = if (tag in selected) selected - tag else selected + tag },
                        label = { Text(stringResource(ExerciseLibrary.tag(tag))) })
                }
            }
        }, confirmButton = { LeafButton(enabled = title.isNotBlank(),
            onClick = {
                val profile = EquipmentProfile(id = editingId ?: java.util.UUID.randomUUID().toString(), name = title.trim(), equipment = selected)
                store.update { it.copy(equipmentProfiles = it.equipmentProfiles.filterNot { row -> row.id == profile.id } + profile, activeEquipmentId = profile.id) }
                creating = false
            }) { Text(stringResource(R.string.common_save)) } },
        dismissButton = { TextButton(onClick = { creating = false }) { Text(stringResource(R.string.common_cancel)) } })
}
