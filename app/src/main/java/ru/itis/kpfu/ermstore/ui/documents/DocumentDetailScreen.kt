package ru.itis.kpfu.ermstore.ui.documents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.itis.kpfu.ermstore.domain.model.DocumentLine
import ru.itis.kpfu.ermstore.domain.model.DocumentStatus
import ru.itis.kpfu.ermstore.domain.model.DocumentType
import ru.itis.kpfu.ermstore.ui.components.ConfirmDialog
import ru.itis.kpfu.ermstore.ui.components.EmptyState
import ru.itis.kpfu.ermstore.ui.components.KeyValueRow
import ru.itis.kpfu.ermstore.ui.components.LoadingState
import ru.itis.kpfu.ermstore.ui.components.ScreenScaffold
import ru.itis.kpfu.ermstore.ui.util.documentStatusLabel
import ru.itis.kpfu.ermstore.ui.util.documentTypeLabel
import ru.itis.kpfu.ermstore.ui.util.formatDate
import ru.itis.kpfu.ermstore.ui.util.formatMoney

@Composable
fun DocumentDetailScreen(
	onBack: () -> Unit,
	viewModel: DocumentDetailViewModel = hiltViewModel()
) {
	val state by viewModel.state.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	var showDeleteDialog by remember { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		viewModel.events.collect { event ->
			when (event) {
				is DocumentDetailEvent.Message -> snackbarHostState.showSnackbar(event.text)
				DocumentDetailEvent.Deleted -> onBack()
			}
		}
	}

	ScreenScaffold(
		title = state.document?.document?.number?.let { "Документ № $it" } ?: "Документ",
		onBack = onBack,
		snackbarHost = { SnackbarHost(snackbarHostState) }
	) { padding ->
		val documentWithLines = state.document
		when {
			state.isLoading -> LoadingState(Modifier.padding(padding))
			documentWithLines == null -> EmptyState(
				text = "Документ не найден",
				modifier = Modifier.padding(padding)
			)
			else -> LazyColumn(
				modifier = Modifier.fillMaxSize().padding(padding),
				contentPadding = PaddingValues(16.dp),
				verticalArrangement = Arrangement.spacedBy(10.dp)
			) {
				item {
					Card(modifier = Modifier.fillMaxWidth()) {
						Column(modifier = Modifier.padding(16.dp)) {
							Text(
								text = documentTypeLabel(documentWithLines.document.type),
								style = MaterialTheme.typography.titleLarge
							)
							KeyValueRow("Статус", documentStatusLabel(documentWithLines.document.status))
							KeyValueRow("Дата", formatDate(documentWithLines.document.dateMillis))
							KeyValueRow("Место хранения", state.locationName.ifBlank { "—" })
							if (documentWithLines.document.type != DocumentType.INVENTORY) {
								KeyValueRow(
									label = "Сумма",
									value = formatMoney(documentWithLines.document.totalKopecks),
									emphasized = true
								)
							} else if (documentWithLines.document.totalKopecks != 0L) {
								KeyValueRow(
									label = "Корректировка",
									value = formatMoney(documentWithLines.document.totalKopecks),
									emphasized = true
								)
							}
						}
					}
				}

				item {
					Text("Строки документа", style = MaterialTheme.typography.titleMedium)
				}

				if (
					documentWithLines.document.type == DocumentType.INVENTORY &&
					documentWithLines.lines.isEmpty()
				) {
					item {
						Text(
							text = "Изменений нет.",
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}

				items(documentWithLines.lines, key = { it.id }) { line ->
					DocumentLineCard(
						line = line,
						type = documentWithLines.document.type,
						status = documentWithLines.document.status
					)
				}

				if (documentWithLines.document.status == DocumentStatus.DRAFT) {
					item {
						Button(
							onClick = viewModel::post,
							enabled = !state.isWorking,
							modifier = Modifier.fillMaxWidth()
						) {
							Text(if (state.isWorking) "Проведение..." else "Провести документ")
						}
					}
					item {
						OutlinedButton(
							onClick = { showDeleteDialog = true },
							enabled = !state.isWorking,
							modifier = Modifier.fillMaxWidth()
						) {
							Text("Удалить черновик")
						}
					}
				} else {
					item {
						Text(
							text = "Проведённый документ нельзя изменить или отменить.",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			}
		}
	}

	if (showDeleteDialog) {
		ConfirmDialog(
			title = "Удалить черновик?",
			text = "Документ и его строки будут удалены.",
			confirmText = "Удалить",
			onConfirm = {
				showDeleteDialog = false
				viewModel.delete()
			},
			onDismiss = { showDeleteDialog = false }
		)
	}
}

@Composable
private fun DocumentLineCard(line: DocumentLine, type: DocumentType, status: DocumentStatus) {
	Card(modifier = Modifier.fillMaxWidth()) {
		Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
			Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
				Text(line.productName, style = MaterialTheme.typography.titleMedium)
				Text("${line.quantityUnits} шт.", style = MaterialTheme.typography.bodyMedium)
			}
			when (type) {
				DocumentType.PURCHASE -> {
					Text(
						text = "Цена: ${formatMoney(line.priceKopecks)} · Сумма: ${formatMoney(line.costKopecks)}",
						style = MaterialTheme.typography.bodySmall
					)
				}
				DocumentType.SALE -> {
					Text(
						text = if (status == DocumentStatus.DRAFT) {
							"Цена: ${formatMoney(line.priceKopecks)}"
						} else {
							"Цена: ${formatMoney(line.priceKopecks)} · COGS: ${formatMoney(line.costKopecks)}"
						},
						style = MaterialTheme.typography.bodySmall
					)
				}
				DocumentType.INVENTORY -> {
					val before = line.beforeUnits
					val after = line.afterUnits
					if (before != null && after != null) {
						Text(
							text = "Было: $before шт. · Стало: $after шт.",
							style = MaterialTheme.typography.bodySmall
						)
					}
					Text(
						text = if (status == DocumentStatus.DRAFT) {
							"Корректировка появится после проведения"
						} else {
							"Корректировка: ${formatMoney(line.costKopecks)}"
						},
						style = MaterialTheme.typography.bodySmall,
						color = if (status == DocumentStatus.DRAFT) {
							MaterialTheme.colorScheme.onSurfaceVariant
						} else {
							MaterialTheme.colorScheme.onSurface
						}
					)
				}
			}
		}
	}
}
