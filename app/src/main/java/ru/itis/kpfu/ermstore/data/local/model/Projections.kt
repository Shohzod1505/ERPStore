package ru.itis.kpfu.ermstore.data.local.model

data class DocumentLineRow(
	val id: Long,
	val documentId: Long,
	val productId: Long,
	val productName: String,
	val quantityUnits: Long,
	val priceKopecks: Long,
	val costKopecks: Long
)

data class ProductStockRow(
	val productId: Long,
	val productName: String,
	val unitsPerBlock: Int,
	val units: Long
)

data class LocationStockRowDb(
	val locationId: Long,
	val locationName: String,
	val parentId: Long?,
	val productId: Long,
	val productName: String,
	val unitsPerBlock: Int,
	val units: Long
)

data class SalesReportRowDb(
	val documentId: Long,
	val number: String,
	val dateMillis: Long,
	val locationName: String,
	val totalKopecks: Long
)

data class InventoryAdjustmentTotals(
	val surplusKopecks: Long,
	val shortageKopecks: Long
)

data class InventoryPrefillRowDb(
	val productId: Long,
	val productName: String,
	val unitsPerBlock: Int,
	val systemUnits: Long
)
