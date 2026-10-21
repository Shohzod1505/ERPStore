package ru.itis.kpfu.ermstore.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.itis.kpfu.ermstore.domain.model.LocationStockRow
import ru.itis.kpfu.ermstore.domain.model.ProductStock
import ru.itis.kpfu.ermstore.domain.model.ProfitAndLoss
import ru.itis.kpfu.ermstore.domain.model.SalesReport
import ru.itis.kpfu.ermstore.domain.usecase.ObserveLocationStockUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveProductStockUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveProfitAndLossUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveSalesReportUseCase
import javax.inject.Inject

data class ReportsUiState(
	val productStock: List<ProductStock> = emptyList(),
	val locationStock: List<LocationStockRow> = emptyList(),
	val salesReport: SalesReport = SalesReport(emptyList(), 0),
	val profitAndLoss: ProfitAndLoss = ProfitAndLoss(
		revenueKopecks = 0,
		cogsKopecks = 0,
		grossProfitKopecks = 0,
		otherIncomeKopecks = 0,
		expensesKopecks = 0,
		netProfitKopecks = 0,
		cashFlowKopecks = 0
	),
	val isLoading: Boolean = true
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
	observeProductStock: ObserveProductStockUseCase,
	observeLocationStock: ObserveLocationStockUseCase,
	observeSalesReport: ObserveSalesReportUseCase,
	observeProfitAndLoss: ObserveProfitAndLossUseCase
) : ViewModel() {
	val state: StateFlow<ReportsUiState> = combine(
		observeProductStock(),
		observeLocationStock(),
		observeSalesReport(),
		observeProfitAndLoss()
	) { productStock, locationStock, salesReport, profitAndLoss ->
		ReportsUiState(
			productStock = productStock,
			locationStock = locationStock,
			salesReport = salesReport,
			profitAndLoss = profitAndLoss,
			isLoading = false
		)
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = ReportsUiState()
	)
}
