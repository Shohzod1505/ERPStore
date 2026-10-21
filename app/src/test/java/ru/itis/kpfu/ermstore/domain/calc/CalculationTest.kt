package ru.itis.kpfu.ermstore.domain.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.itis.kpfu.ermstore.domain.model.CashFilter
import ru.itis.kpfu.ermstore.domain.model.CashOperationType

class CalculationTest {
	@Test
	fun `weighted average uses incoming price when there is no stock`() {
		assertEquals(1500L, WeightedAverageCost.calculate(0, 1000, 10, 1500))
	}

	@Test
	fun `weighted average mixes old and incoming value`() {
		assertEquals(1250L, WeightedAverageCost.calculate(10, 1000, 10, 1500))
	}

	@Test
	fun `weighted average rounds to nearest kopeck`() {
		assertEquals(101L, WeightedAverageCost.calculate(1, 100, 2, 101))
	}

	@Test
	fun `profit and loss excludes loans and includes inventory adjustments`() {
		val result = ProfitAndLossCalculator.calculate(
			revenueKopecks = 10_000,
			cogsKopecks = 6_000,
			manualIncomeKopecks = 500,
			inventorySurplusKopecks = 200,
			manualExpenseKopecks = 300,
			inventoryShortageKopecks = 100,
			cashFlowKopecks = -1_234
		)

		assertEquals(4_000, result.grossProfitKopecks)
		assertEquals(700, result.otherIncomeKopecks)
		assertEquals(400, result.expensesKopecks)
		assertEquals(4_300, result.netProfitKopecks)
		assertEquals(-1_234, result.cashFlowKopecks)
	}

	@Test
	fun `cash signs follow operation type`() {
		assertEquals(500, CashMath.signedAmount(CashOperationType.INCOME, 500))
		assertEquals(-500, CashMath.signedAmount(CashOperationType.EXPENSE, 500))
		assertEquals(-500, CashMath.signedAmount(CashOperationType.DEBT_GIVEN, 500))
		assertEquals(500, CashMath.signedAmount(CashOperationType.DEBT_RECEIVED, 500))
		assertEquals(500, CashMath.signedAmount(CashOperationType.DEBT_REPAID_TO_US, 500))
		assertEquals(-500, CashMath.signedAmount(CashOperationType.DEBT_REPAID_BY_US, 500))
	}

	@Test
	fun `cash filters separate ordinary operations from debts`() {
		assertTrue(CashFilter.ALL.matches(CashOperationType.INCOME))
		assertTrue(CashFilter.INCOME.matches(CashOperationType.INCOME))
		assertFalse(CashFilter.INCOME.matches(CashOperationType.DEBT_REPAID_TO_US))
		assertTrue(CashFilter.EXPENSE.matches(CashOperationType.EXPENSE))
		assertFalse(CashFilter.EXPENSE.matches(CashOperationType.DEBT_REPAID_BY_US))
		assertTrue(CashFilter.DEBT.matches(CashOperationType.DEBT_GIVEN))
		assertTrue(CashFilter.DEBT.matches(CashOperationType.DEBT_RECEIVED))
		assertTrue(CashFilter.DEBT.matches(CashOperationType.DEBT_REPAID_TO_US))
		assertTrue(CashFilter.DEBT.matches(CashOperationType.DEBT_REPAID_BY_US))
	}

	@Test
	fun `cash spend limit allows exact balance`() {
		assertTrue(CashMath.canSpend(500, 500))
		assertTrue(CashMath.canSpend(500, 400))
		assertFalse(CashMath.canSpend(500, 501))
	}

	@Test
	fun `blocks split into full blocks and remainder`() {
		assertEquals(2, StockMath.blocks(25, 12))
		assertEquals(1, StockMath.remainderUnits(25, 12))
		assertEquals(0, StockMath.blocks(0, 12))
		assertEquals(0, StockMath.remainderUnits(0, 12))
	}

	@Test
	fun `inventory before units restores previous count`() {
		assertEquals(100, StockMath.inventoryBeforeUnits(120, 20))
		assertEquals(120, StockMath.inventoryBeforeUnits(100, -20))
	}

	@Test
	fun `line total multiplies quantity by price`() {
		assertEquals(12_000, MoneyMath.lineTotal(12, 1_000))
	}
}
