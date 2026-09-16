package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LeafEditSheet(onDismissRequest: () -> Unit, title: (@Composable () -> Unit)? = null,
    confirmButton: @Composable () -> Unit, dismissButton: (@Composable () -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null) {
    ModalBottomSheet(onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp) {
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 24.dp)) {
            if (title != null) {
                ProvideTextStyle(MaterialTheme.typography.headlineSmall) { title() }
                Spacer(Modifier.height(20.dp))
            }
            if (content != null) Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), content = content)
            FlowRow(Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dismissButton?.invoke(); confirmButton()
            }
        }
    }
}
