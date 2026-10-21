package ru.itis.kpfu.ermstore.ui.locations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.itis.kpfu.ermstore.ui.components.AddButton
import ru.itis.kpfu.ermstore.ui.components.EmptyState
import ru.itis.kpfu.ermstore.ui.components.LoadingState
import ru.itis.kpfu.ermstore.ui.components.ScreenScaffold
import ru.itis.kpfu.ermstore.ui.components.SelectionField
import ru.itis.kpfu.ermstore.ui.util.formatQuantity

@Composable
fun LocationsScreen(viewModel: LocationsViewModel = hiltViewModel()) {
	val state by viewModel.state.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	var showAddDialog by remember { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		viewModel.events.collect { snackbarHostState.showSnackbar(it) }
	}

	ScreenScaffold(
		title = "Склады",
		floatingActionButton = { AddButton(onClick = { showAddDialog = true }) },
		snackbarHost = { SnackbarHost(snackbarHostState) }
	) { padding ->
		when {
			state.isLoading -> LoadingState(Modifier.padding(padding))
			state.rows.isEmpty() -> EmptyState(
				text = "Складов пока нет. Создайте склад, затем подсклад.",
				modifier = Modifier.padding(padding)
			)
			else -> LazyColumn(
				modifier = Modifier.fillMaxSize().padding(padding),
				contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				items(state.rows, key = { it.id }) { location ->
					LocationCard(location)
				}
			}
		}
	}

	if (showAddDialog) {
		AddLocationDialog(
			warehouses = state.warehouseOptions,
			onDismiss = { showAddDialog = false },
			onConfirm = { name, parentId ->
				viewModel.create(name, parentId)
				showAddDialog = false
			}
		)
	}
}

@Composable
private fun LocationCard(location: LocationRowUi) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(start = if (location.parentId == null) 0.dp else 24.dp)
	) {
		Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
			Text(
				text = if (location.parentId == null) location.name else "↳ ${location.name}",
				style = MaterialTheme.typography.titleMedium,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			if (location.parentName != null) {
				Text(
					text = "Подсклад склада <${location.parentName}>",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
			if (location.stockLines.isEmpty()) {
				Text(
					text = "Остатков нет",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			} else {
				location.stockLines.forEach { line ->
					Text(
						text = "${line.productName}: ${formatQuantity(line.units, line.unitsPerBlock)}",
						style = MaterialTheme.typography.bodySmall,
						maxLines = 2,
						overflow = TextOverflow.Ellipsis
					)
				}
			}
		}
	}
}

@Composable
private fun AddLocationDialog(
	warehouses: List<Pair<Long, String>>,
	onDismiss: () -> Unit,
	onConfirm: (String, Long?) -> Unit
) {
	var name by remember { mutableStateOf("") }
	var isSubwarehouse by remember { mutableStateOf(false) }
	var parentId by remember { mutableStateOf<Long?>(warehouses.firstOrNull()?.first) }
	var error by remember { mutableStateOf<String?>(null) }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("Новый склад и подсклад") },
		text = {
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				OutlinedTextField(
					value = name,
					onValueChange = { name = it },
					label = { Text("Название") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true
				)
				Row(verticalAlignment = Alignment.CenterVertically) {
					Checkbox(
						checked = isSubwarehouse,
						onCheckedChange = { checked ->
							isSubwarehouse = checked
							if (checked) {
								parentId = parentId ?: warehouses.firstOrNull()?.first
							}
						}
					)
					Text("Это подсклад")
				}
				if (isSubwarehouse) {
					SelectionField(
						label = "Основной склад",
						selectedText = warehouses.firstOrNull { it.first == parentId }?.second,
						options = warehouses,
						onSelected = { parentId = it },
						enabled = warehouses.isNotEmpty()
					)
					if (warehouses.isEmpty()) {
						Text(
							text = "Сначала создайте склад.",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.error
						)
					}
				} else {
					Text(
						text = "Подсклад «Основное хранилище» будет создан автоматически.",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
				if (error != null) {
					Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
				}
			}
		},
		confirmButton = {
			TextButton(
				onClick = {
					when {
						name.isBlank() -> error = "Введите название"
						isSubwarehouse && warehouses.isEmpty() -> error = "Сначала создайте склад"
						isSubwarehouse && parentId == null -> error = "Выберите основной склад"
						else -> onConfirm(name, if (isSubwarehouse) parentId else null)
					}
				}
			) {
				Text("Создать")
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text("Отмена")
			}
		}
	)
}
