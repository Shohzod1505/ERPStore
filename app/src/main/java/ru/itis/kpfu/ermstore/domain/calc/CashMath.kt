package ru.itis.kpfu.ermstore.domain.calc

import ru.itis.kpfu.ermstore.domain.model.CashOperationType

object CashMath {
	fun signedAmount(type: CashOperationType, amountKopecks: Long): Long {
		require(amountKopecks >= 0) { "Amount must not be negative" }
		return type.cashSign * amountKopecks
	}

	fun canSpend(balanceKopecks: Long, amountKopecks: Long): Boolean =
		amountKopecks <= balanceKopecks
}
