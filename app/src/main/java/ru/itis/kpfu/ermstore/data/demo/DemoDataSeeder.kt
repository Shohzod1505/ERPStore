package ru.itis.kpfu.ermstore.data.demo

import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.itis.kpfu.ermstore.domain.model.CashOperationType
import ru.itis.kpfu.ermstore.domain.model.CreateDocumentInput
import ru.itis.kpfu.ermstore.domain.model.DocumentType
import ru.itis.kpfu.ermstore.domain.model.DraftLineInput
import ru.itis.kpfu.ermstore.domain.model.PostingResult
import ru.itis.kpfu.ermstore.domain.usecase.AddCashOperationUseCase
import ru.itis.kpfu.ermstore.domain.usecase.CreateDraftUseCase
import ru.itis.kpfu.ermstore.domain.usecase.CreateLocationUseCase
import ru.itis.kpfu.ermstore.domain.usecase.CreateProductUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveLocationsUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveProductsUseCase
import ru.itis.kpfu.ermstore.domain.usecase.PostDocumentUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoDataSeeder @Inject constructor(
	private val observeProducts: ObserveProductsUseCase,
	private val observeLocations: ObserveLocationsUseCase,
	private val createProduct: CreateProductUseCase,
	private val createLocation: CreateLocationUseCase,
	private val createDraft: CreateDraftUseCase,
	private val postDocument: PostDocumentUseCase,
	private val addCashOperation: AddCashOperationUseCase
) {
	private val mutex = Mutex()

	suspend fun seedIfEmpty() = mutex.withLock {
		val products = observeProducts().first()
		val locations = observeLocations().first()
		if (products.isNotEmpty() || locations.isNotEmpty()) return@withLock

		runCatching {
			seed()
		}.onFailure { error ->
			Log.e("DemoDataSeeder", "Failed to create demo data", error)
		}
	}

	private suspend fun seed() {
		val colaId = createProduct("Coca-Cola 0.5 л", 12)
		val pepsiId = createProduct("Pepsi 0.5 л", 12)
		val waterId = createProduct("Вода «Святой источник» 0.5 л", 12)
		val juiceId = createProduct("Сок «Добрый» апельсин 1 л", 6)
		val energyId = createProduct("Энергетик Adrenaline Rush 0.449 л", 24)

		val mainWarehouseId = createLocation("Основной склад", null)
		val fridgeId = createLocation("Холодильник", mainWarehouseId)
		val backroomId = createLocation("Подсобка", null)

		postPurchase(
			locationId = mainWarehouseId,
			lines = listOf(
				DraftLineInput(colaId, 120, 4_500),
				DraftLineInput(pepsiId, 60, 4_000),
				DraftLineInput(waterId, 240, 2_500)
			)
		)
		postPurchase(
			locationId = fridgeId,
			lines = listOf(
				DraftLineInput(colaId, 48, 4_800),
				DraftLineInput(energyId, 48, 7_000),
				DraftLineInput(juiceId, 24, 8_000)
			)
		)
		postPurchase(
			locationId = backroomId,
			lines = listOf(
				DraftLineInput(pepsiId, 48, 4_200),
				DraftLineInput(juiceId, 12, 8_200)
			)
		)

		postSale(
			locationId = mainWarehouseId,
			lines = listOf(
				DraftLineInput(colaId, 24, 7_000),
				DraftLineInput(waterId, 12, 4_500)
			)
		)
		postSale(
			locationId = fridgeId,
			lines = listOf(
				DraftLineInput(colaId, 12, 8_000),
				DraftLineInput(energyId, 6, 12_000),
				DraftLineInput(juiceId, 4, 13_000)
			)
		)

		postInventory(
			locationId = mainWarehouseId,
			countedUnits = mapOf(
				colaId to 100L,
				pepsiId to 60L,
				waterId to 220L,
				juiceId to 0L,
				energyId to 0L
			)
		)

		addCashOperation(CashOperationType.DEBT_RECEIVED, 3_000_000, "Заём на закупку")
		addCashOperation(CashOperationType.INCOME, 50_000, "Прочий доход")
		addCashOperation(CashOperationType.EXPENSE, 35_000, "Доставка")
		addCashOperation(CashOperationType.DEBT_GIVEN, 100_000, "Займ сотруднику")
	}

	private suspend fun postPurchase(locationId: Long, lines: List<DraftLineInput>) {
		val documentId = createDraft(
			CreateDocumentInput(
				type = DocumentType.PURCHASE,
				locationId = locationId,
				lines = lines
			)
		)
		check(postDocument(documentId) is PostingResult.Success) {
			"Failed to post demo purchase $documentId"
		}
	}

	private suspend fun postSale(locationId: Long, lines: List<DraftLineInput>) {
		val documentId = createDraft(
			CreateDocumentInput(
				type = DocumentType.SALE,
				locationId = locationId,
				lines = lines
			)
		)
		check(postDocument(documentId) is PostingResult.Success) {
			"Failed to post demo sale $documentId"
		}
	}

	private suspend fun postInventory(locationId: Long, countedUnits: Map<Long, Long>) {
		val products = observeProducts().first()
		val documentId = createDraft(
			CreateDocumentInput(
				type = DocumentType.INVENTORY,
				locationId = locationId,
				lines = products.map { product ->
					DraftLineInput(
						productId = product.id,
						quantityUnits = countedUnits[product.id] ?: 0,
						priceKopecks = 0
					)
				}
			)
		)
		check(postDocument(documentId) is PostingResult.Success) {
			"Failed to post demo inventory $documentId"
		}
	}
}
