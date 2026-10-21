package ru.itis.kpfu.ermstore.ui.documents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.itis.kpfu.ermstore.domain.model.DocumentStatus
import ru.itis.kpfu.ermstore.domain.model.DocumentType
import ru.itis.kpfu.ermstore.ui.components.AddButton
import ru.itis.kpfu.ermstore.ui.components.EmptyState
import ru.itis.kpfu.ermstore.ui.components.LoadingState
import ru.itis.kpfu.ermstore.ui.components.ScreenScaffold
import ru.itis.kpfu.ermstore.ui.util.documentStatusLabel
import ru.itis.kpfu.ermstore.ui.util.documentTypeLabel
import ru.itis.kpfu.ermstore.ui.util.formatDate
import ru.itis.kpfu.ermstore.ui.util.formatMoney

@Composable
fun DocumentsScreen(
	onAdd: (DocumentType) -> Unit,
	onOpen: (Long) -> Unit,
	viewModel: DocumentsViewModel = hiltViewModel()
) {
	val state by viewModel.state.collectAsState()
	val types = listOf(DocumentType.PURCHASE, DocumentType.SALE, DocumentType.INVENTORY)

	ScreenScaffold(
		title = "Документы",
		floatingActionButton = { AddButton(onClick = { onAdd(state.type) }) }
	) { padding ->
		Column(modifier = Modifier.fillMaxSize().padding(padding)) {
			PrimaryTabRow(selectedTabIndex = types.indexOf(state.type)) {
				types.forEach { type ->
					Tab(
						selected = state.type == type,
						onClick = { viewModel.selectType(type) },
						text = { Text(documentTypeLabel(type)) }
					)
				}
			}
			when {
				state.isLoading -> LoadingState()
				state.rows.isEmpty() -> EmptyState(
					text = "Документов этого типа пока нет. Нажмите «+», чтобы создать черновик."
				)
				else -> LazyColumn(
					modifier = Modifier.fillMaxSize(),
					contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					items(state.rows, key = { it.id }) { document ->
						DocumentCard(
							document = document,
							onClick = { onOpen(document.id) }
						)
					}
				}
			}
		}
	}
}

@Composable
private fun DocumentCard(document: DocumentRowUi, onClick: () -> Unit) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
	) {
		Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
			Text(
				text = "№ ${document.number}",
				style = MaterialTheme.typography.titleMedium
			)
			Text(
				text = "${documentTypeLabel(document.type)} · ${documentStatusLabel(document.status)}",
				style = MaterialTheme.typography.bodySmall,
				color = when (document.status) {
					DocumentStatus.DRAFT -> MaterialTheme.colorScheme.tertiary
					DocumentStatus.POSTED -> MaterialTheme.colorScheme.primary
					DocumentStatus.CANCELLED -> MaterialTheme.colorScheme.error
				}
			)
			Text(
				text = "${formatDate(document.dateMillis)} · ${document.locationName}",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			if (document.type != DocumentType.INVENTORY || document.totalKopecks != 0L) {
				Text(
					text = if (document.type == DocumentType.INVENTORY) {
						"Корректировка: ${formatMoney(document.totalKopecks)}"
					} else {
						"Сумма: ${formatMoney(document.totalKopecks)}"
					},
					style = MaterialTheme.typography.bodyMedium
				)
			}
		}
	}
}
