package ru.itis.kpfu.ermstore.data.local.mapper

import ru.itis.kpfu.ermstore.data.local.entity.CashTransactionEntity
import ru.itis.kpfu.ermstore.data.local.entity.DocumentEntity
import ru.itis.kpfu.ermstore.data.local.entity.LocationEntity
import ru.itis.kpfu.ermstore.data.local.entity.ProductEntity
import ru.itis.kpfu.ermstore.data.local.model.DocumentLineRow
import ru.itis.kpfu.ermstore.data.local.model.InventoryPrefillRowDb
import ru.itis.kpfu.ermstore.data.local.model.LocationStockRowDb
import ru.itis.kpfu.ermstore.data.local.model.ProductStockRow
import ru.itis.kpfu.ermstore.data.local.model.SalesReportRowDb
import ru.itis.kpfu.ermstore.domain.model.CashTransaction
import ru.itis.kpfu.ermstore.domain.model.Document
import ru.itis.kpfu.ermstore.domain.model.DocumentLine
import ru.itis.kpfu.ermstore.domain.model.InventoryPrefillRow
import ru.itis.kpfu.ermstore.domain.model.LocationStockRow
import ru.itis.kpfu.ermstore.domain.model.Product
import ru.itis.kpfu.ermstore.domain.model.ProductStock
import ru.itis.kpfu.ermstore.domain.model.SalesReportRow
import ru.itis.kpfu.ermstore.domain.model.StorageLocation

fun ProductEntity.toDomain(): Product = Product(
	id = id,
	name = name,
	unitsPerBlock = unitsPerBlock,
	averageCostKopecks = averageCostKopecks
)

fun LocationEntity.toDomain(): StorageLocation = StorageLocation(
	id = id,
	name = name,
	parentId = parentId
)

fun DocumentEntity.toDomain(): Document = Document(
	id = id,
	type = type,
	status = status,
	number = number,
	dateMillis = dateMillis,
	locationId = locationId,
	totalKopecks = totalKopecks
)

fun DocumentLineRow.toDomain(): DocumentLine = DocumentLine(
	id = id,
	documentId = documentId,
	productId = productId,
	productName = productName,
	quantityUnits = quantityUnits,
	priceKopecks = priceKopecks,
	costKopecks = costKopecks
)

fun CashTransactionEntity.toDomain(): CashTransaction = CashTransaction(
	id = id,
	type = type,
	amountKopecks = amountKopecks,
	dateMillis = dateMillis,
	comment = comment,
	documentId = documentId
)

fun ProductStockRow.toDomain(): ProductStock = ProductStock(
	productId = productId,
	productName = productName,
	unitsPerBlock = unitsPerBlock,
	units = units
)

fun LocationStockRowDb.toDomain(): LocationStockRow = LocationStockRow(
	locationId = locationId,
	locationName = locationName,
	parentId = parentId,
	productId = productId,
	productName = productName,
	unitsPerBlock = unitsPerBlock,
	units = units
)

fun SalesReportRowDb.toDomain(): SalesReportRow = SalesReportRow(
	documentId = documentId,
	number = number,
	dateMillis = dateMillis,
	locationName = locationName,
	totalKopecks = totalKopecks
)

fun InventoryPrefillRowDb.toDomain(): InventoryPrefillRow = InventoryPrefillRow(
	productId = productId,
	productName = productName,
	unitsPerBlock = unitsPerBlock,
	systemUnits = systemUnits
)
