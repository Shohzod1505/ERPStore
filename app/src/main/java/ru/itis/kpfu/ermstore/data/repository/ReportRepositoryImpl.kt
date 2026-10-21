package ru.itis.kpfu.ermstore.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import ru.itis.kpfu.ermstore.data.local.dao.CashTransactionDao
import ru.itis.kpfu.ermstore.data.local.dao.DocumentDao
import ru.itis.kpfu.ermstore.data.local.dao.DocumentLineDao
import ru.itis.kpfu.ermstore.data.local.dao.ProductDao
import ru.itis.kpfu.ermstore.data.local.dao.StockMovementDao
import ru.itis.kpfu.ermstore.data.local.mapper.toDomain
import ru.itis.kpfu.ermstore.domain.calc.ProfitAndLossCalculator
import ru.itis.kpfu.ermstore.domain.model.LocationStockRow
import ru.itis.kpfu.ermstore.domain.model.ProductStock
import ru.itis.kpfu.ermstore.domain.model.ProfitAndLoss
import ru.itis.kpfu.ermstore.domain.model.SalesReport
import ru.itis.kpfu.ermstore.domain.repository.ReportRepository
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
	private val productDao: ProductDao,
	private val stockMovementDao: StockMovementDao,
	private val documentDao: DocumentDao,
	private val documentLineDao: DocumentLineDao,
	private val cashTransactionDao: CashTransactionDao
) : ReportRepository {
	override fun observeProductStock(): Flow<List<ProductStock>> =
		productDao.observeProductStock().map { rows -> rows.map { it.toDomain() } }

	override fun observeLocationStock(): Flow<List<LocationStockRow>> =
		stockMovementDao.observeLocationStock().map { rows -> rows.map { it.toDomain() } }

	override fun observeSalesReport(): Flow<SalesReport> =
		documentDao.observeSalesReportRows().map { rows ->
			val reportRows = rows.map { it.toDomain() }
			SalesReport(
				rows = reportRows,
				totalKopecks = reportRows.sumOf { it.totalKopecks }
			)
		}

	override fun observeProfitAndLoss(): Flow<ProfitAndLoss> {
		val base = combine(
			documentDao.observePostedSalesTotal(),
			documentLineDao.observeCogs(),
			cashTransactionDao.observeManualIncome(),
			cashTransactionDao.observeManualExpense(),
			documentLineDao.observeInventoryAdjustments()
		) { revenue, cogs, manualIncome, manualExpense, adjustments ->
			PnlBase(
				revenueKopecks = revenue,
				cogsKopecks = cogs,
				manualIncomeKopecks = manualIncome,
				manualExpenseKopecks = manualExpense,
				inventorySurplusKopecks = adjustments.surplusKopecks,
				inventoryShortageKopecks = adjustments.shortageKopecks
			)
		}

		return base.combine(cashTransactionDao.observeCashFlow()) { values, cashFlow ->
			ProfitAndLossCalculator.calculate(
				revenueKopecks = values.revenueKopecks,
				cogsKopecks = values.cogsKopecks,
				manualIncomeKopecks = values.manualIncomeKopecks,
				inventorySurplusKopecks = values.inventorySurplusKopecks,
				manualExpenseKopecks = values.manualExpenseKopecks,
				inventoryShortageKopecks = values.inventoryShortageKopecks,
				cashFlowKopecks = cashFlow
			)
		}
	}

	private data class PnlBase(
		val revenueKopecks: Long,
		val cogsKopecks: Long,
		val manualIncomeKopecks: Long,
		val manualExpenseKopecks: Long,
		val inventorySurplusKopecks: Long,
		val inventoryShortageKopecks: Long
	)
}
