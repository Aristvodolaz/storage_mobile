package com.komus.sorage_mobile.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.komus.sorage_mobile.data.db.entity.PendingOperationEntity
import com.komus.sorage_mobile.data.offline.OfflineOpType
import com.komus.sorage_mobile.domain.viewModel.AppViewModel

/** Счётчик неотправленных операций и кнопка с отклонёнными операциями. */
@Composable
fun OfflineQueueStatus(appViewModel: AppViewModel = hiltViewModel()) {
    val pending by appViewModel.pendingCount.collectAsState()
    val failed by appViewModel.failedOperations.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (pending > 0) {
            Text(text = "В очереди: $pending", style = MaterialTheme.typography.caption)
        }
        if (failed.isNotEmpty()) {
            TextButton(onClick = { showDialog = true }) {
                Text(text = "Ошибки: ${failed.size}", color = MaterialTheme.colors.error)
            }
        }
    }

    if (showDialog) {
        FailedOperationsDialog(
            operations = failed,
            onRetry = { appViewModel.retryOperation(it) },
            onDiscard = { appViewModel.discardOperation(it) },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun FailedOperationsDialog(
    operations: List<PendingOperationEntity>,
    onRetry: (Long) -> Unit,
    onDiscard: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Не отправленные операции") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(operations, key = { it.id }) { op ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(text = operationTitle(op.type), style = MaterialTheme.typography.subtitle1)
                        Text(
                            text = op.lastError ?: "Сервер отклонил операцию",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.error
                        )
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { onDiscard(op.id) }) { Text("Удалить") }
                            TextButton(onClick = { onRetry(op.id) }) { Text("Повторить") }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}

private fun operationTitle(type: String): String = when (runCatching { OfflineOpType.valueOf(type) }.getOrNull()) {
    OfflineOpType.PLACE_BUFFER -> "Размещение"
    OfflineOpType.MOVE -> "Перемещение"
    OfflineOpType.PICK_PRODUCT, OfflineOpType.PICK_LOCATION, OfflineOpType.PICK_LOCATION_SKLAD -> "Отбор"
    null -> "Операция"
}
