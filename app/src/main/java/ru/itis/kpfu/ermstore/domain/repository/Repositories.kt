package ru.itis.kpfu.ermstore.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.itis.kpfu.ermstore.domain.model.CashOperationType
import ru.itis.kpfu.ermstore.domain.model.CashTransaction
import ru.itis.kpfu.ermstore.domain.model.CreateDocumentInput
import ru.itis.kpfu.ermstore.domain.model.Document
import ru.itis.kpfu.ermstore.domain.model.DocumentType
import ru.itis.kpfu.ermstore.domain.model.DocumentWithLines
import ru.itis.kpfu.ermstore.domain.model.InventoryPrefillRow
import ru.itis.kpfu.ermstore.domain.model.LocationStockRow
import ru.itis.kpfu.ermstore.domain.model.PostingResult
import ru.itis.kpfu.ermstore.domain.model.Product
import ru.itis.kpfu.ermstore.domain.model.ProductStock
import ru.itis.kpfu.ermstore.domain.model.ProfitAndLoss
import ru.itis.kpfu.ermstore.domain.model.SalesReport
import ru.itis.kpfu.ermstore.domain.model.StorageLocation

interface ProductRepository {
	fun observeProducts(): Flow<List<Product>>
	suspend fun getProduct(id: Long): Product?
	suspend fun createProduct(name: String, unitsPerBlock: Int): Long
}

interface LocationRepository {
	fun observeLocations(): Flow<List<StorageLocation>>
	suspend fun createLocation(name: String, parentId: Long?): Long
}

interface DocumentRepository {
	fun observeDocuments(type: DocumentType): Flow<List<Document>>
	suspend fun getDocument(id: Long): DocumentWithLines?
	suspend fun createDraft(input: CreateDocumentInput): Long
	suspend fun postDocument(id: Long): PostingResult
	suspend fun deleteDraft(id: Long)
	fun observeInventoryPrefill(locationId: Long): Flow<List<InventoryPrefillRow>>
}

interface CashRepository {
	fun observeTransactions(): Flow<List<CashTransaction>>
	fun observeBalance(): Flow<Long>
	suspend fun addManual(type: CashOperationType, amountKopecks: Long, comment: String?)
}

interface ReportRepository {
	fun observeProductStock(): Flow<List<ProductStock>>
	fun observeLocationStock(): Flow<List<LocationStockRow>>
	fun observeSalesReport(): Flow<SalesReport>
	fun observeProfitAndLoss(): Flow<ProfitAndLoss>
}
