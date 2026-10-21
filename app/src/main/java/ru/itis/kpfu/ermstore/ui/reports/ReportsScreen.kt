package ru.itis.kpfu.ermstore.ui.reports

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
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.itis.kpfu.ermstore.domain.model.LocationStockRow
import ru.itis.kpfu.ermstore.ui.components.EmptyState
import ru.itis.kpfu.ermstore.ui.components.KeyValueRow
import ru.itis.kpfu.ermstore.ui.components.LoadingState
import ru.itis.kpfu.ermstore.ui.components.ScreenScaffold
import ru.itis.kpfu.ermstore.ui.util.formatDate
import ru.itis.kpfu.ermstore.ui.util.formatMoney
import ru.itis.kpfu.ermstore.ui.util.formatQuantity

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
	val state by viewModel.state.collectAsState()
	val tabs = listOf("Остатки", "Продажи", "P&L")
	var selectedTab by remember { mutableIntStateOf(0) }

	ScreenScaffold(title = "Отчёты") { padding ->
		Column(modifier = Modifier.fillMaxSize().padding(padding)) {
			PrimaryScrollableTabRow(
				selectedTabIndex = selectedTab,
				edgePadding = 0.dp
			) {
				tabs.forEachIndexed { index, title ->
					Tab(
						selected = selectedTab == index,
						onClick = { selectedTab = index },
						text = {
							Text(
								text = title,
								maxLines = 1,
								softWrap = false
							)
						}
					)
				}
			}
			when {
				state.isLoading -> LoadingState()
				selectedTab == 0 -> StockReport(state)
				selectedTab == 1 -> SalesReportContent(state)
				else -> ProfitAndLossContent(state)
			}
		}
	}
}

@Composable
private fun StockReport(state: ReportsUiState) {
	val byWarehouse = state.locationStock.groupBy { it.parentName ?: it.locationName }
	LazyColumn(
		modifier = Modifier.fillMaxSize(),
		contentPadding = PaddingValues(16.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		item {
			Text("Итого по товарам", style = MaterialTheme.typography.titleMedium)
		}
		if (state.productStock.isEmpty()) {
			item {
				EmptyState(text = "Товаров пока нет")
			}
		} else {
			items(state.productStock, key = { it.productId }) { product ->
				Card(modifier = Modifier.fillMaxWidth()) {
					Column(modifier = Modifier.padding(12.dp)) {
						Text(product.productName, style = MaterialTheme.typography.titleSmall)
						Text(
							text = formatQuantity(product.units, product.unitsPerBlock),
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			}
		}

		item {
			Text("По складам", style = MaterialTheme.typography.titleMedium)
		}
		if (byWarehouse.isEmpty()) {
			item {
				Text(
					text = "Остатков пока нет",
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		} else {
			byWarehouse.forEach { (warehouseName, rows) ->
				item(key = "warehouse-$warehouseName") {
					WarehouseStockCard(warehouseName, rows)
				}
			}
		}
	}
}

@Composable
private fun WarehouseStockCard(warehouseName: String, rows: List<LocationStockRow>) {
	val totals = rows
		.groupBy { it.productId }
		.values
		.map { productRows ->
			val first = productRows.first()
			first.copy(units = productRows.sumOf { it.units })
		}
		.sortedBy { it.productName }
	Card(modifier = Modifier.fillMaxWidth()) {
		Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
			Text(warehouseName, style = MaterialTheme.typography.titleSmall)
			totals.forEach { row ->
				Text(
					text = "Итого ${row.productName}: ${formatQuantity(row.units, row.unitsPerBlock)}",
					style = MaterialTheme.typography.bodySmall
				)
			}
			rows.groupBy { it.locationId }.values.forEach { childRows ->
				val child = childRows.first()
				Text(
					text = child.locationName,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				childRows.forEach { row ->
					Text(
						text = "${row.productName}: ${formatQuantity(row.units, row.unitsPerBlock)}",
						style = MaterialTheme.typography.bodySmall
					)
				}
			}
		}
	}
}

@Composable
private fun SalesReportContent(state: ReportsUiState) {
	LazyColumn(
		modifier = Modifier.fillMaxSize(),
		contentPadding = PaddingValues(16.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		item {
			Card(modifier = Modifier.fillMaxWidth()) {
				Column(modifier = Modifier.padding(16.dp)) {
					Text(
						text = "Итого продаж",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Text(
						text = formatMoney(state.salesReport.totalKopecks),
						style = MaterialTheme.typography.headlineSmall
					)
				}
			}
		}
		if (state.salesReport.rows.isEmpty()) {
			item {
				EmptyState(text = "Проведённых продаж пока нет")
			}
		} else {
			items(state.salesReport.rows, key = { it.documentId }) { row ->
				Card(modifier = Modifier.fillMaxWidth()) {
					Column(modifier = Modifier.padding(12.dp)) {
						Text("№ ${row.number}", style = MaterialTheme.typography.titleSmall)
						Text(
							text = "${formatDate(row.dateMillis)} · ${row.locationName}",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
						Text(formatMoney(row.totalKopecks), style = MaterialTheme.typography.bodyMedium)
					}
				}
			}
		}
	}
}

@Composable
private fun ProfitAndLossContent(state: ReportsUiState) {
	val pnl = state.profitAndLoss
	LazyColumn(
		modifier = Modifier.fillMaxSize(),
		contentPadding = PaddingValues(16.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		item {
			Card(modifier = Modifier.fillMaxWidth()) {
				Column(modifier = Modifier.padding(16.dp)) {
					Text("Прибыли и убытки", style = MaterialTheme.typography.titleMedium)
					KeyValueRow("Выручка", formatMoney(pnl.revenueKopecks))
					KeyValueRow("COGS", formatMoney(pnl.cogsKopecks))
					KeyValueRow("Валовая прибыль", formatMoney(pnl.grossProfitKopecks))
					KeyValueRow("Прочие доходы", formatMoney(pnl.otherIncomeKopecks))
					KeyValueRow("Расходы", formatMoney(pnl.expensesKopecks))
					KeyValueRow(
						label = "Чистая прибыль",
						value = formatMoney(pnl.netProfitKopecks),
						emphasized = true
					)
				}
			}
		}
		item {
			Card(modifier = Modifier.fillMaxWidth()) {
				Column(modifier = Modifier.padding(16.dp)) {
					Text("Денежный поток", style = MaterialTheme.typography.titleMedium)
					KeyValueRow(
						label = "Все операции, включая займы",
						value = formatMoney(pnl.cashFlowKopecks),
						emphasized = true
					)
				}
			}
		}
	}
}
