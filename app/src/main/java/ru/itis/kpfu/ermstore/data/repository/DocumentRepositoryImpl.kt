package ru.itis.kpfu.ermstore.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.itis.kpfu.ermstore.data.local.ErmStoreDatabase
import ru.itis.kpfu.ermstore.data.local.dao.CashTransactionDao
import ru.itis.kpfu.ermstore.data.local.dao.DocumentDao
import ru.itis.kpfu.ermstore.data.local.dao.DocumentLineDao
import ru.itis.kpfu.ermstore.data.local.dao.LocationDao
import ru.itis.kpfu.ermstore.data.local.dao.ProductDao
import ru.itis.kpfu.ermstore.data.local.dao.StockMovementDao
import ru.itis.kpfu.ermstore.data.local.entity.CashTransactionEntity
import ru.itis.kpfu.ermstore.data.local.entity.DocumentEntity
import ru.itis.kpfu.ermstore.data.local.entity.DocumentLineEntity
import ru.itis.kpfu.ermstore.data.local.entity.StockMovementEntity
import ru.itis.kpfu.ermstore.data.local.entity.StockMovementType
import ru.itis.kpfu.ermstore.data.local.mapper.toDomain
import ru.itis.kpfu.ermstore.data.local.model.DocumentLineRow
import ru.itis.kpfu.ermstore.domain.calc.CashMath
import ru.itis.kpfu.ermstore.domain.calc.MoneyMath
import ru.itis.kpfu.ermstore.domain.calc.StockMath
import ru.itis.kpfu.ermstore.domain.calc.WeightedAverageCost
import ru.itis.kpfu.ermstore.domain.model.CashOperationType
import ru.itis.kpfu.ermstore.domain.model.CreateDocumentInput
import ru.itis.kpfu.ermstore.domain.model.Document
import ru.itis.kpfu.ermstore.domain.model.DocumentStatus
import ru.itis.kpfu.ermstore.domain.model.DocumentType
import ru.itis.kpfu.ermstore.domain.model.DocumentWithLines
import ru.itis.kpfu.ermstore.domain.model.InventoryPrefillRow
import ru.itis.kpfu.ermstore.domain.model.PostingError
import ru.itis.kpfu.ermstore.domain.model.PostingResult
import ru.itis.kpfu.ermstore.domain.repository.DocumentRepository
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
	private val database: ErmStoreDatabase,
	private val documentDao: DocumentDao,
	private val documentLineDao: DocumentLineDao,
	private val productDao: ProductDao,
	private val locationDao: LocationDao,
	private val stockMovementDao: StockMovementDao,
	private val cashTransactionDao: CashTransactionDao
) : DocumentRepository {
	override fun observeDocuments(type: DocumentType): Flow<List<Document>> =
		documentDao.observeByType(type).map { documents -> documents.map { it.toDomain() } }

	override suspend fun getDocument(id: Long): DocumentWithLines? {
		val document = documentDao.getById(id) ?: return null
		val rows = documentLineDao.getRows(id)
		val lines = if (document.type == DocumentType.INVENTORY) {
			val movements = if (document.status == DocumentStatus.POSTED) {
				stockMovementDao.getForDocument(id).associateBy { it.productId }
			} else {
				emptyMap()
			}
			rows.mapNotNull { row ->
				val beforeUnits = if (document.status == DocumentStatus.POSTED) {
					StockMath.inventoryBeforeUnits(
						countedUnits = row.quantityUnits,
						deltaUnits = movements[row.productId]?.quantityDelta ?: 0
					)
				} else {
					stockMovementDao.getStockAtLocation(row.productId, document.locationId)
				}
				val afterUnits = row.quantityUnits
				if (beforeUnits == afterUnits) {
					null
				} else {
					row.toDomain().copy(
						beforeUnits = beforeUnits,
						afterUnits = afterUnits
					)
				}
			}
		} else {
			rows.map { it.toDomain() }
		}
		return DocumentWithLines(
			document = document.toDomain(),
			lines = lines
		)
	}

	override suspend fun createDraft(input: CreateDocumentInput): Long {
		require(input.lines.isNotEmpty()) { "Document must contain at least one line" }
		val location = requireNotNull(locationDao.getById(input.locationId)) { "Location does not exist" }
		require(location.parentId != null) { "Operations are allowed only for subwarehouses" }
		input.lines.forEach { line ->
			require(if (input.type == DocumentType.INVENTORY) line.quantityUnits >= 0 else line.quantityUnits > 0) {
				"Quantity is invalid"
			}
			require(line.priceKopecks >= 0) { "Price must not be negative" }
			requireNotNull(productDao.getById(line.productId)) { "Product does not exist" }
		}

		return database.withTransaction {
			val dateMillis = System.currentTimeMillis()
			val count = documentDao.countByType(input.type) + 1
			val number = "${prefixFor(input.type)}-${count.toString().padStart(4, '0')}"
			val draftTotal = if (input.type == DocumentType.INVENTORY) {
				0L
			} else {
				input.lines.fold(0L) { sum, line ->
					Math.addExact(sum, MoneyMath.lineTotal(line.quantityUnits, line.priceKopecks))
				}
			}
			val documentId = documentDao.insert(
				DocumentEntity(
					type = input.type,
					status = DocumentStatus.DRAFT,
					number = number,
					dateMillis = dateMillis,
					locationId = input.locationId,
					totalKopecks = draftTotal
				)
			)
			documentLineDao.insertAll(
				input.lines.map { line ->
					DocumentLineEntity(
						documentId = documentId,
						productId = line.productId,
						quantityUnits = line.quantityUnits,
						priceKopecks = if (input.type == DocumentType.INVENTORY) 0 else line.priceKopecks,
						costKopecks = if (input.type == DocumentType.PURCHASE) {
							MoneyMath.lineTotal(line.quantityUnits, line.priceKopecks)
						} else {
							0
						}
					)
				}
			)
			documentId
		}
	}

	override suspend fun postDocument(id: Long): PostingResult {
		return try {
			database.withTransaction {
				postInTransaction(id)
			}
			PostingResult.Success
		} catch (error: PostingException) {
			PostingResult.Failure(error.error)
		}
	}

	override suspend fun deleteDraft(id: Long) {
		documentDao.deleteDraft(id)
	}

	override fun observeInventoryPrefill(locationId: Long): Flow<List<InventoryPrefillRow>> =
		stockMovementDao.observeInventoryPrefill(locationId).map { rows ->
			rows.map { it.toDomain() }
		}

	private suspend fun postInTransaction(id: Long) {
		val document = documentDao.getById(id)
			?: throw PostingException(PostingError.DocumentNotFound)
		if (document.status != DocumentStatus.DRAFT) {
			throw PostingException(PostingError.DocumentIsNotDraft)
		}
		val location = locationDao.getById(document.locationId)
		if (location?.parentId == null) {
			throw PostingException(PostingError.InvalidLocation)
		}

		val lines = documentLineDao.getRows(id)
		if (lines.isEmpty()) {
			throw PostingException(PostingError.EmptyDocument)
		}

		val total = when (document.type) {
			DocumentType.PURCHASE -> postPurchase(document, lines)
			DocumentType.SALE -> postSale(document, lines)
			DocumentType.INVENTORY -> postInventory(document, lines)
		}

		documentDao.updateStatusAndTotal(
			id = document.id,
			status = DocumentStatus.POSTED,
			totalKopecks = total
		)

		when (document.type) {
			DocumentType.PURCHASE -> addCash(
				document = document,
				type = CashOperationType.EXPENSE,
				amountKopecks = total,
				label = "Закупка"
			)
			DocumentType.SALE -> addCash(
				document = document,
				type = CashOperationType.INCOME,
				amountKopecks = total,
				label = "Продажа"
			)
			DocumentType.INVENTORY -> Unit
		}
	}

	private suspend fun postPurchase(document: DocumentEntity, lines: List<DocumentLineRow>): Long {
		var total = 0L
		for (line in lines) {
			validateLine(DocumentType.PURCHASE, line)
			val product = productDao.getById(line.productId)
				?: throw PostingException(PostingError.InvalidLine(line.productName))

			val oldUnits = stockMovementDao.getTotalStock(line.productId)
			val newAverageCost = WeightedAverageCost.calculate(
				oldUnits = oldUnits,
				oldAverageCostKopecks = product.averageCostKopecks,
				incomingUnits = line.quantityUnits,
				incomingPriceKopecks = line.priceKopecks
			)

			stockMovementDao.insert(
				StockMovementEntity(
					productId = line.productId,
					locationId = document.locationId,
					quantityDelta = line.quantityUnits,
					unitCostKopecks = line.priceKopecks,
					documentId = document.id,
					type = StockMovementType.PURCHASE,
					dateMillis = document.dateMillis
				)
			)
			productDao.updateAverageCost(line.productId, newAverageCost)

			val lineTotal = MoneyMath.lineTotal(line.quantityUnits, line.priceKopecks)
			documentLineDao.updateCost(line.id, lineTotal)
			total = Math.addExact(total, lineTotal)
		}
		return total
	}

	private suspend fun postSale(document: DocumentEntity, lines: List<DocumentLineRow>): Long {
		lines.forEach { validateLine(DocumentType.SALE, it) }

		val requestedByProduct = lines.groupBy { it.productId }
			.mapValues { (_, productLines) -> productLines.sumOf { it.quantityUnits } }
		requestedByProduct.forEach { (productId, requestedUnits) ->
			val product = productDao.getById(productId)
				?: throw PostingException(PostingError.InvalidLine(""))
			val availableUnits = stockMovementDao.getStockAtLocation(productId, document.locationId)
			if (availableUnits < requestedUnits) {
				throw PostingException(
					PostingError.InsufficientStock(
						productName = product.name,
						availableUnits = availableUnits,
						requestedUnits = requestedUnits
					)
				)
			}
		}

		var total = 0L
		for (line in lines) {
			val product = productDao.getById(line.productId)
				?: throw PostingException(PostingError.InvalidLine(line.productName))
			val unitCost = product.averageCostKopecks
			val cost = Math.multiplyExact(line.quantityUnits, unitCost)

			stockMovementDao.insert(
				StockMovementEntity(
					productId = line.productId,
					locationId = document.locationId,
					quantityDelta = -line.quantityUnits,
					unitCostKopecks = unitCost,
					documentId = document.id,
					type = StockMovementType.SALE,
					dateMillis = document.dateMillis
				)
			)
			documentLineDao.updateCost(line.id, cost)
			total = Math.addExact(total, MoneyMath.lineTotal(line.quantityUnits, line.priceKopecks))
		}
		return total
	}

	private suspend fun postInventory(document: DocumentEntity, lines: List<DocumentLineRow>): Long {
		var total = 0L
		for (line in lines) {
			validateLine(DocumentType.INVENTORY, line)
			val product = productDao.getById(line.productId)
				?: throw PostingException(PostingError.InvalidLine(line.productName))
			val currentUnits = stockMovementDao.getStockAtLocation(line.productId, document.locationId)
			val delta = line.quantityUnits - currentUnits
			val unitCost = product.averageCostKopecks
			val adjustmentValue = Math.multiplyExact(delta, unitCost)

			if (delta != 0L) {
				stockMovementDao.insert(
					StockMovementEntity(
						productId = line.productId,
						locationId = document.locationId,
						quantityDelta = delta,
						unitCostKopecks = unitCost,
						documentId = document.id,
						type = StockMovementType.INVENTORY,
						dateMillis = document.dateMillis
					)
				)
			}
			documentLineDao.updateCost(line.id, adjustmentValue)
			total = Math.addExact(total, adjustmentValue)
		}
		return total
	}

	private suspend fun addCash(
		document: DocumentEntity,
		type: CashOperationType,
		amountKopecks: Long,
		label: String
	) {
		if (amountKopecks <= 0) return
		if (type.cashSign < 0) {
			val balance = cashTransactionDao.getBalance()
			if (!CashMath.canSpend(balance, amountKopecks)) {
				throw PostingException(
					PostingError.InsufficientCash(
						availableKopecks = balance,
						requestedKopecks = amountKopecks
					)
				)
			}
		}
		cashTransactionDao.insert(
			CashTransactionEntity(
				type = type,
				amountKopecks = amountKopecks,
				dateMillis = document.dateMillis,
				comment = "$label №${document.number}",
				documentId = document.id
			)
		)
	}

	private fun validateLine(type: DocumentType, line: DocumentLineRow) {
		val validQuantity = when (type) {
			DocumentType.INVENTORY -> line.quantityUnits >= 0
			DocumentType.PURCHASE, DocumentType.SALE -> line.quantityUnits > 0
		}
		if (!validQuantity || line.priceKopecks < 0) {
			throw PostingException(PostingError.InvalidLine(line.productName))
		}
	}

	private fun prefixFor(type: DocumentType): String = when (type) {
		DocumentType.PURCHASE -> "ЗК"
		DocumentType.SALE -> "ПР"
		DocumentType.INVENTORY -> "ИН"
	}

	private class PostingException(val error: PostingError) : Exception()
}
