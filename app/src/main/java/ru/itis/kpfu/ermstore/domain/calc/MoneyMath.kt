package ru.itis.kpfu.ermstore.domain.calc

object MoneyMath {
	fun lineTotal(quantityUnits: Long, priceKopecks: Long): Long {
		require(quantityUnits >= 0) { "Quantity must not be negative" }
		require(priceKopecks >= 0) { "Price must not be negative" }
		return Math.multiplyExact(quantityUnits, priceKopecks)
	}
}
