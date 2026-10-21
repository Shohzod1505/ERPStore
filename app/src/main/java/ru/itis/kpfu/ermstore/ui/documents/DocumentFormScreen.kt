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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.itis.kpfu.ermstore.domain.model.DocumentType
import ru.itis.kpfu.ermstore.ui.components.ScreenScaffold
import ru.itis.kpfu.ermstore.ui.components.SelectionField
import ru.itis.kpfu.ermstore.ui.util.documentTypeLabel
import ru.itis.kpfu.ermstore.ui.util.formatQuantity

@Composable
fun DocumentFormScreen(
	onBack: () -> Unit,
	onSaved: (Long) -> Unit,
	viewModel: DocumentFormViewModel = hiltViewModel()
) {
	val state by viewModel.state.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }

	LaunchedEffect(Unit) {
		viewModel.events.collect { event ->
			when (event) {
				is DocumentFormEvent.Saved -> onSaved(event.documentId)
				is DocumentFormEvent.Message -> snackbarHostState.showSnackbar(event.text)
			}
		}
	}

	ScreenScaffold(
		title = "Новый документ: ${documentTypeLabel(state.type)}",
		onBack = onBack,
		snackbarHost = { SnackbarHost(snackbarHostState) }
	) { padding ->
		LazyColumn(
			modifier = Modifier.fillMaxSize().padding(padding),
			contentPadding = PaddingValues(16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			item {
				LocationPicker(state = state, onSelect = viewModel::selectLocation)
			}

			when (state.type) {
				DocumentType.INVENTORY -> {
					if (state.selectedLocationId != null) {
						if (state.inventoryLines.isEmpty()) {
							item {
								Text(
									text = "Для инвентаризации сначала добавьте товары.",
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}
						} else {
							item {
								Text(
									text = "Фактическое количество по всем товарам",
									style = MaterialTheme.typography.titleMedium
								)
							}
							items(state.inventoryLines, key = { it.productId }) { line ->
								InventoryLineCard(
									line = line,
									onCountChange = { value ->
										viewModel.updateInventoryCount(line.productId, value)
									}
								)
							}
						}
					}
				}

				DocumentType.PURCHASE, DocumentType.SALE -> {
					item {
						val availableProducts = state.products.filter { product ->
							state.lines.none { it.productId == product.id }
						}
						if (state.products.isEmpty()) {
							Text(
								text = "Сначала добавьте товары в разделе «Товары».",
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						} else {
							SelectionField(
								label = "Добавить товар",
								selectedText = null,
								options = availableProducts.map { it.id to it.name },
								onSelected = { viewModel.addLine(it) }
							)
						}
					}
					itemsIndexed(state.lines, key = { _, line -> line.productId }) { index, line ->
						PurchaseSaleLineCard(
							line = line,
							onQuantityChange = { value -> viewModel.updateLineQuantity(index, value) },
							onPriceChange = { value -> viewModel.updateLinePrice(index, value) },
							onRemove = { viewModel.removeLine(index) }
						)
					}
				}
			}

			if (state.error != null) {
				item {
					Text(
						text = state.error.orEmpty(),
						color = MaterialTheme.colorScheme.error
					)
				}
			}

			item {
				Button(
					onClick = viewModel::save,
					enabled = !state.isSaving && state.selectedLocationId != null,
					modifier = Modifier.fillMaxWidth()
				) {
					Text(if (state.isSaving) "Сохранение..." else "Сохранить черновик")
				}
			}
		}
	}
}

@Composable
private fun LocationPicker(state: DocumentFormUiState, onSelect: (Long) -> Unit) {
	val names = state.locations.associate { it.id to it.name }
	val options = state.locations.map { location ->
		val label = if (location.parentId == null) {
			location.name
		} else {
			"${names[location.parentId] ?: "Склад"} / ${location.name}"
		}
		location.id to label
	}
	Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
		Text("Место хранения", style = MaterialTheme.typography.titleMedium)
		SelectionField(
			label = "Выберите склад или подсклад",
			selectedText = options.firstOrNull { it.first == state.selectedLocationId }?.second,
			options = options,
			onSelected = onSelect
		)
		if (state.locations.isEmpty()) {
			Text(
				text = "Складов пока нет. Создайте их в разделе «Склады».",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

@Composable
private fun PurchaseSaleLineCard(
	line: PurchaseSaleLineUi,
	onQuantityChange: (String) -> Unit,
	onPriceChange: (String) -> Unit,
	onRemove: () -> Unit
) {
	Card(modifier = Modifier.fillMaxWidth()) {
		Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
			Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
				Text(line.productName, style = MaterialTheme.typography.titleMedium)
				TextButton(onClick = onRemove) {
					Text("Удалить")
				}
			}
			Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				OutlinedTextField(
					value = line.quantity,
					onValueChange = { onQuantityChange(it.filter(Char::isDigit)) },
					label = { Text("Количество") },
					modifier = Modifier.weight(1f),
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
				)
				OutlinedTextField(
					value = line.price,
					onValueChange = { onPriceChange(it.filter { char -> char.isDigit() || char == ',' || char == '.' }) },
					label = { Text("Цена, ₽") },
					modifier = Modifier.weight(1f),
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
				)
			}
		}
	}
}

@Composable
private fun InventoryLineCard(
	line: InventoryLineUi,
	onCountChange: (String) -> Unit
) {
	Card(modifier = Modifier.fillMaxWidth()) {
		Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
			Text(line.productName, style = MaterialTheme.typography.titleMedium)
			Text(
				text = "В системе: ${formatQuantity(line.systemUnits, line.unitsPerBlock)}",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			OutlinedTextField(
				value = line.counted,
				onValueChange = { onCountChange(it.filter(Char::isDigit)) },
				label = { Text("Фактическое количество, шт.") },
				modifier = Modifier.fillMaxWidth(),
				singleLine = true,
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
			)
		}
	}
}
