package ru.itis.kpfu.ermstore.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.itis.kpfu.ermstore.domain.model.CashOperationType
import ru.itis.kpfu.ermstore.domain.model.DocumentStatus
import ru.itis.kpfu.ermstore.domain.model.DocumentType

@Entity(tableName = "products")
data class ProductEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val name: String,
	val unitsPerBlock: Int,
	val averageCostKopecks: Long = 0
)

@Entity(tableName = "locations")
data class LocationEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val name: String,
	val parentId: Long? = null
)

@Entity(
	tableName = "documents",
	foreignKeys = [
		ForeignKey(
			entity = LocationEntity::class,
			parentColumns = ["id"],
			childColumns = ["locationId"]
		)
	],
	indices = [Index("locationId"), Index("type", "status")]
)
data class DocumentEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val type: DocumentType,
	val status: DocumentStatus,
	val number: String,
	val dateMillis: Long,
	val locationId: Long,
	val totalKopecks: Long = 0
)

@Entity(
	tableName = "document_lines",
	foreignKeys = [
		ForeignKey(
			entity = DocumentEntity::class,
			parentColumns = ["id"],
			childColumns = ["documentId"],
			onDelete = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = ProductEntity::class,
			parentColumns = ["id"],
			childColumns = ["productId"]
		)
	],
	indices = [Index("documentId"), Index("productId")]
)
data class DocumentLineEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val documentId: Long,
	val productId: Long,
	val quantityUnits: Long,
	val priceKopecks: Long = 0,
	val costKopecks: Long = 0
)

enum class StockMovementType {
	PURCHASE,
	SALE,
	INVENTORY
}

@Entity(
	tableName = "stock_movements",
	foreignKeys = [
		ForeignKey(
			entity = DocumentEntity::class,
			parentColumns = ["id"],
			childColumns = ["documentId"],
			onDelete = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = ProductEntity::class,
			parentColumns = ["id"],
			childColumns = ["productId"]
		),
		ForeignKey(
			entity = LocationEntity::class,
			parentColumns = ["id"],
			childColumns = ["locationId"]
		)
	],
	indices = [
		Index("documentId"),
		Index("productId", "locationId"),
		Index("locationId")
	]
)
data class StockMovementEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val productId: Long,
	val locationId: Long,
	val quantityDelta: Long,
	val unitCostKopecks: Long,
	val documentId: Long,
	val type: StockMovementType,
	val dateMillis: Long
)

@Entity(
	tableName = "cash_transactions",
	foreignKeys = [
		ForeignKey(
			entity = DocumentEntity::class,
			parentColumns = ["id"],
			childColumns = ["documentId"],
			onDelete = ForeignKey.SET_NULL
		)
	],
	indices = [Index("documentId")]
)
data class CashTransactionEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val type: CashOperationType,
	val amountKopecks: Long,
	val dateMillis: Long,
	val comment: String? = null,
	val documentId: Long? = null
)
