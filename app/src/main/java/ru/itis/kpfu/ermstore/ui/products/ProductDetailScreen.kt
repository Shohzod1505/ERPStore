package ru.itis.kpfu.ermstore.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.itis.kpfu.ermstore.domain.model.LocationStockRow
import ru.itis.kpfu.ermstore.domain.model.ProductMovement
import ru.itis.kpfu.ermstore.ui.components.EmptyState
import ru.itis.kpfu.ermstore.ui.components.LoadingState
import ru.itis.kpfu.ermstore.ui.components.ScreenScaffold
import ru.itis.kpfu.ermstore.ui.util.documentTypeLabel
import ru.itis.kpfu.ermstore.ui.util.formatDate
import ru.itis.kpfu.ermstore.ui.util.formatMoney
import ru.itis.kpfu.ermstore.ui.util.formatQuantity
import ru.itis.kpfu.ermstore.ui.util.formatSignedUnits

@Composable
fun ProductDetailScreen(
	onBack: () -> Unit,
	viewModel: ProductDetailViewModel = hiltViewModel()
) {
	val state by viewModel.state.collectAsState()

	ScreenScaffold(
		title = state.product?.name ?: "Товар",
		onBack = onBack
	) { padding ->
		when {
			state.isLoading -> LoadingState(Modifier.padding(padding))
			state.product == null -> EmptyState(
				text = "Товар не найден",
				modifier = Modifier.padding(padding)
			)
			else -> {
				val product = state.product
				val totalUnits = state.stockByLocation.sumOf { it.units }
				LazyColumn(
					modifier = Modifier.fillMaxSize().padding(padding),
					contentPadding = PaddingValues(16.dp),
					verticalArrangement = Arrangement.spacedBy(10.dp)
				) {
					item {
						Card(modifier = Modifier.fillMaxWidth()) {
							Column(
								modifier = Modifier.padding(16.dp),
								verticalArrangement = Arrangement.spacedBy(4.dp)
							) {
								Text(
									text = product?.name.orEmpty(),
									style = MaterialTheme.typography.titleLarge,
									maxLines = 2,
									overflow = TextOverflow.Ellipsis
								)
								Text(
									text = formatQuantity(totalUnits, product?.unitsPerBlock ?: 1),
									style = MaterialTheme.typography.bodyLarge
								)
								Text(
									text = "Средняя себестоимость: ${formatMoney(product?.averageCostKopecks ?: 0)}",
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}
						}
					}

					item {
						Text("Остатки по складам", style = MaterialTheme.typography.titleMedium)
					}
					if (state.stockByLocation.isEmpty()) {
						item {
							Text(
								text = "Остатков нет",
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					} else {
						item {
							StockByWarehouseCard(state.stockByLocation)
						}
					}

					item {
						Text("История движений", style = MaterialTheme.typography.titleMedium)
					}
					if (state.movements.isEmpty()) {
						item {
							Text(
								text = "Движений пока нет",
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					} else {
						items(state.movements, key = { it.id }) { movement ->
							MovementCard(movement)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun StockByWarehouseCard(rows: List<LocationStockRow>) {
	val byWarehouse = rows.groupBy { it.parentName ?: it.locationName }
	Card(modifier = Modifier.fillMaxWidth()) {
		Column(
			modifier = Modifier.padding(12.dp),
			verticalArrangement = Arrangement.spacedBy(6.dp)
		) {
			byWarehouse.forEach { (warehouseName, warehouseRows) ->
				val total = warehouseRows.sumOf { it.units }
				val unitsPerBlock = warehouseRows.first().unitsPerBlock
				Text(warehouseName, style = MaterialTheme.typography.titleSmall)
				Text(
					text = "Итого: ${formatQuantity(total, unitsPerBlock)}",
					style = MaterialTheme.typography.bodySmall
				)
				warehouseRows.forEach { row ->
					Text(
						text = "${row.locationName}: ${formatQuantity(row.units, row.unitsPerBlock)}",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
		}
	}
}

@Composable
private fun MovementCard(movement: ProductMovement) {
	Card(modifier = Modifier.fillMaxWidth()) {
		Column(
			modifier = Modifier.padding(12.dp),
			verticalArrangement = Arrangement.spacedBy(3.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				Text(
					text = "${documentTypeLabel(movement.documentType)} №${movement.documentNumber}",
					style = MaterialTheme.typography.titleSmall,
					modifier = Modifier.weight(1f),
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Text(
					text = formatSignedUnits(movement.quantityDelta),
					style = MaterialTheme.typography.titleSmall,
					color = if (movement.quantityDelta >= 0) {
						MaterialTheme.colorScheme.primary
					} else {
						MaterialTheme.colorScheme.error
					}
				)
			}
			Text(
				text = formatDate(movement.dateMillis),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			val location = movement.parentName?.let { "${it} / ${movement.locationName}" }
				?: movement.locationName
			Text(
				text = location,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Text(
				text = "Себестоимость: ${formatMoney(movement.unitCostKopecks)}",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}
