package ru.itis.kpfu.ermstore.domain.calc

object WeightedAverageCost {
	fun calculate(
		oldUnits: Long,
		oldAverageCostKopecks: Long,
		incomingUnits: Long,
		incomingPriceKopecks: Long
	): Long {
		require(incomingUnits > 0) { "Incoming quantity must be positive" }
		require(incomingPriceKopecks >= 0) { "Price must not be negative" }
		if (oldUnits <= 0) return incomingPriceKopecks

		val totalUnits = Math.addExact(oldUnits, incomingUnits)
		val oldValue = Math.multiplyExact(oldUnits, oldAverageCostKopecks)
		val incomingValue = Math.multiplyExact(incomingUnits, incomingPriceKopecks)
		val totalValue = Math.addExact(oldValue, incomingValue)
		return (totalValue + totalUnits / 2) / totalUnits
	}
}
