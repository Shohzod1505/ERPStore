package ru.itis.kpfu.ermstore.domain.model

enum class DocumentType {
	PURCHASE,
	SALE,
	INVENTORY
}

enum class DocumentStatus {
	DRAFT,
	POSTED,
	CANCELLED
}

const val DEFAULT_SUBWAREHOUSE_NAME = "Основное хранилище"

enum class CashOperationType(
	val cashSign: Int,
	val isLoan: Boolean
) {
	INCOME(1, false),
	EXPENSE(-1, false),
	DEBT_GIVEN(-1, true),
	DEBT_RECEIVED(1, true),
	DEBT_REPAID_TO_US(1, true),
	DEBT_REPAID_BY_US(-1, true)
}

enum class CashFilter {
	ALL,
	INCOME,
	EXPENSE,
	DEBT;

	fun matches(type: CashOperationType): Boolean = when (this) {
		ALL -> true
		INCOME -> !type.isLoan && type.cashSign > 0
		EXPENSE -> !type.isLoan && type.cashSign < 0
		DEBT -> type.isLoan
	}
}

sealed interface CashOperationError {
	data object InvalidAmount : CashOperationError
	data class InsufficientFunds(
		val availableKopecks: Long,
		val requestedKopecks: Long
	) : CashOperationError
}

sealed interface CashOperationResult {
	data object Success : CashOperationResult
	data class Failure(val error: CashOperationError) : CashOperationResult
}

data class Product(
	val id: Long = 0,
	val name: String,
	val unitsPerBlock: Int,
	val averageCostKopecks: Long = 0
)

data class StorageLocation(
	val id: Long = 0,
	val name: String,
	val parentId: Long? = null
)

data class Document(
	val id: Long = 0,
	val type: DocumentType,
	val status: DocumentStatus,
	val number: String,
	val dateMillis: Long,
	val locationId: Long,
	val totalKopecks: Long
)

data class DocumentLine(
	val id: Long = 0,
	val documentId: Long,
	val productId: Long,
	val productName: String,
	val quantityUnits: Long,
	val priceKopecks: Long,
	val costKopecks: Long,
	val beforeUnits: Long? = null,
	val afterUnits: Long? = null
)

data class DocumentWithLines(
	val document: Document,
	val lines: List<DocumentLine>
)

data class CashTransaction(
	val id: Long = 0,
	val type: CashOperationType,
	val amountKopecks: Long,
	val dateMillis: Long,
	val comment: String? = null,
	val documentId: Long? = null
)

data class DraftLineInput(
	val productId: Long,
	val quantityUnits: Long,
	val priceKopecks: Long = 0
)

data class CreateDocumentInput(
	val type: DocumentType,
	val locationId: Long,
	val lines: List<DraftLineInput>
)

data class InventoryPrefillRow(
	val productId: Long,
	val productName: String,
	val unitsPerBlock: Int,
	val systemUnits: Long
)
