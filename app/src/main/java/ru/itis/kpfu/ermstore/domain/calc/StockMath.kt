package ru.itis.kpfu.ermstore.domain.calc

object StockMath {
	fun blocks(units: Long, unitsPerBlock: Int): Long {
		require(unitsPerBlock > 0) { "Units per block must be positive" }
		return units / unitsPerBlock
	}

	fun remainderUnits(units: Long, unitsPerBlock: Int): Long {
		require(unitsPerBlock > 0) { "Units per block must be positive" }
		return units % unitsPerBlock
	}
}
