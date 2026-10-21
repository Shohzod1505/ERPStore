package ru.itis.kpfu.ermstore.domain.calc

import ru.itis.kpfu.ermstore.domain.model.ProfitAndLoss

object ProfitAndLossCalculator {
	fun calculate(
		revenueKopecks: Long,
		cogsKopecks: Long,
		manualIncomeKopecks: Long,
		inventorySurplusKopecks: Long,
		manualExpenseKopecks: Long,
		inventoryShortageKopecks: Long,
		cashFlowKopecks: Long
	): ProfitAndLoss {
		val grossProfit = revenueKopecks - cogsKopecks
		val otherIncome = manualIncomeKopecks + inventorySurplusKopecks
		val expenses = manualExpenseKopecks + inventoryShortageKopecks
		return ProfitAndLoss(
			revenueKopecks = revenueKopecks,
			cogsKopecks = cogsKopecks,
			grossProfitKopecks = grossProfit,
			otherIncomeKopecks = otherIncome,
			expensesKopecks = expenses,
			netProfitKopecks = grossProfit + otherIncome - expenses,
			cashFlowKopecks = cashFlowKopecks
		)
	}
}
