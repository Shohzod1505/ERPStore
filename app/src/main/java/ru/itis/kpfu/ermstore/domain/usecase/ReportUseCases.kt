package ru.itis.kpfu.ermstore.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.itis.kpfu.ermstore.domain.model.LocationStockRow
import ru.itis.kpfu.ermstore.domain.model.ProductStock
import ru.itis.kpfu.ermstore.domain.model.ProfitAndLoss
import ru.itis.kpfu.ermstore.domain.model.SalesReport
import ru.itis.kpfu.ermstore.domain.repository.ReportRepository
import javax.inject.Inject

class ObserveProductStockUseCase @Inject constructor(
	private val repository: ReportRepository
) {
	operator fun invoke(): Flow<List<ProductStock>> = repository.observeProductStock()
}

class ObserveLocationStockUseCase @Inject constructor(
	private val repository: ReportRepository
) {
	operator fun invoke(): Flow<List<LocationStockRow>> = repository.observeLocationStock()
}

class ObserveSalesReportUseCase @Inject constructor(
	private val repository: ReportRepository
) {
	operator fun invoke(): Flow<SalesReport> = repository.observeSalesReport()
}

class ObserveProfitAndLossUseCase @Inject constructor(
	private val repository: ReportRepository
) {
	operator fun invoke(): Flow<ProfitAndLoss> = repository.observeProfitAndLoss()
}
