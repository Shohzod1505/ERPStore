package ru.itis.kpfu.ermstore.domain.model

data class ProductStock(
	val productId: Long,
	val productName: String,
	val unitsPerBlock: Int,
	val units: Long
) {
	val blocks: Long get() = units / unitsPerBlock
	val remainderUnits: Long get() = units % unitsPerBlock
}

data class LocationStockRow(
	val locationId: Long,
	val locationName: String,
	val parentId: Long?,
	val parentName: String?,
	val productId: Long,
	val productName: String,
	val unitsPerBlock: Int,
	val units: Long
) {
	val blocks: Long get() = units / unitsPerBlock
	val remainderUnits: Long get() = units % unitsPerBlock
}

data class ProductMovement(
	val id: Long,
	val dateMillis: Long,
	val quantityDelta: Long,
	val unitCostKopecks: Long,
	val documentId: Long,
	val documentNumber: String,
	val documentType: DocumentType,
	val locationName: String,
	val parentName: String?
)

data class ProductDetail(
	val product: Product,
	val stockByLocation: List<LocationStockRow>,
	val movements: List<ProductMovement>
)

data class SalesReportRow(
	val documentId: Long,
	val number: String,
	val dateMillis: Long,
	val locationName: String,
	val totalKopecks: Long
)

data class SalesReport(
	val rows: List<SalesReportRow>,
	val totalKopecks: Long
)

data class ProfitAndLoss(
	val revenueKopecks: Long,
	val cogsKopecks: Long,
	val grossProfitKopecks: Long,
	val otherIncomeKopecks: Long,
	val expensesKopecks: Long,
	val netProfitKopecks: Long,
	val cashFlowKopecks: Long
)

sealed interface PostingError {
	data class InsufficientStock(
		val productName: String,
		val availableUnits: Long,
		val requestedUnits: Long
	) : PostingError

	data object DocumentNotFound : PostingError
	data object DocumentIsNotDraft : PostingError
	data object EmptyDocument : PostingError
	data object InvalidLocation : PostingError
	data class InvalidLine(val productName: String) : PostingError
	data class InsufficientCash(
		val availableKopecks: Long,
		val requestedKopecks: Long
	) : PostingError
}

sealed interface PostingResult {
	data object Success : PostingResult
	data class Failure(val error: PostingError) : PostingResult
}
