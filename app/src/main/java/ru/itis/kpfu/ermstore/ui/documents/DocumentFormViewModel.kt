package ru.itis.kpfu.ermstore.ui.documents

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.kpfu.ermstore.domain.model.CreateDocumentInput
import ru.itis.kpfu.ermstore.domain.model.DocumentType
import ru.itis.kpfu.ermstore.domain.model.DraftLineInput
import ru.itis.kpfu.ermstore.domain.model.InventoryPrefillRow
import ru.itis.kpfu.ermstore.domain.model.LocationStockRow
import ru.itis.kpfu.ermstore.domain.model.Product
import ru.itis.kpfu.ermstore.domain.model.StorageLocation
import ru.itis.kpfu.ermstore.domain.usecase.CreateDraftUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveInventoryPrefillUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveLocationStockUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveLocationsUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveProductsUseCase
import ru.itis.kpfu.ermstore.ui.util.parseMoneyToKopecks
import ru.itis.kpfu.ermstore.ui.util.parseUnits
import ru.itis.kpfu.ermstore.ui.util.stockAtLocation
import javax.inject.Inject

data class PurchaseSaleLineUi(
	val productId: Long,
	val productName: String,
	val quantity: String = "",
	val price: String = ""
)

data class InventoryLineUi(
	val productId: Long,
	val productName: String,
	val unitsPerBlock: Int,
	val systemUnits: Long,
	val counted: String
)

data class DocumentFormUiState(
	val type: DocumentType = DocumentType.PURCHASE,
	val locations: List<StorageLocation> = emptyList(),
	val products: List<Product> = emptyList(),
	val stock: List<LocationStockRow> = emptyList(),
	val selectedLocationId: Long? = null,
	val lines: List<PurchaseSaleLineUi> = emptyList(),
	val inventoryLines: List<InventoryLineUi> = emptyList(),
	val isSaving: Boolean = false,
	val error: String? = null
)

sealed interface DocumentFormEvent {
	data class Saved(val documentId: Long) : DocumentFormEvent
	data class Message(val text: String) : DocumentFormEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DocumentFormViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	observeLocations: ObserveLocationsUseCase,
	observeProducts: ObserveProductsUseCase,
	observeLocationStock: ObserveLocationStockUseCase,
	private val observeInventoryPrefill: ObserveInventoryPrefillUseCase,
	private val createDraft: CreateDraftUseCase
) : ViewModel() {
	private val type = runCatching {
		DocumentType.valueOf(savedStateHandle.get<String>("type").orEmpty())
	}.getOrDefault(DocumentType.PURCHASE)

	private val _state = MutableStateFlow(DocumentFormUiState(type = type))
	val state: StateFlow<DocumentFormUiState> = _state.asStateFlow()

	private val _events = MutableSharedFlow<DocumentFormEvent>(extraBufferCapacity = 1)
	val events = _events.asSharedFlow()

	init {
		viewModelScope.launch {
			observeLocations().collect { locations ->
				_state.update { it.copy(locations = locations) }
			}
		}
		viewModelScope.launch {
			observeProducts().collect { products ->
				_state.update { it.copy(products = products) }
			}
		}
		viewModelScope.launch {
			observeLocationStock().collect { stock ->
				_state.update { it.copy(stock = stock) }
			}
		}
		viewModelScope.launch {
			_state
				.map { it.selectedLocationId }
				.distinctUntilChanged()
				.flatMapLatest { locationId ->
					_state.update { it.copy(inventoryLines = emptyList()) }
					if (type == DocumentType.INVENTORY && locationId != null) {
						observeInventoryPrefill(locationId)
					} else {
						flowOf(emptyList())
					}
				}
				.collect { rows ->
					_state.update { current ->
						current.copy(
							inventoryLines = rows.map { row ->
								row.toUi(current.inventoryLines)
							}
						)
					}
				}
		}
	}

	fun selectLocation(locationId: Long) {
		val location = _state.value.locations.firstOrNull { it.id == locationId } ?: return
		if (location.parentId == null) {
			_events.tryEmit(DocumentFormEvent.Message("Операции проводятся только по подскладам"))
			return
		}
		_state.update { it.copy(selectedLocationId = locationId, error = null) }
	}

	fun addLine(productId: Long) {
		val current = _state.value
		val product = current.products.firstOrNull { it.id == productId } ?: return
		if (current.lines.any { it.productId == productId }) {
			_events.tryEmit(DocumentFormEvent.Message("Товар уже добавлен"))
			return
		}
		if (
			current.type == DocumentType.SALE &&
			current.selectedLocationId != null &&
			stockAtLocation(current.stock, current.selectedLocationId, productId) <= 0
		) {
			_events.tryEmit(DocumentFormEvent.Message("На выбранном складе нет остатка товара"))
			return
		}
		_state.update {
			it.copy(
				lines = it.lines + PurchaseSaleLineUi(
					productId = product.id,
					productName = product.name
				)
			)
		}
	}

	fun updateLineQuantity(index: Int, value: String) {
		_state.update { current ->
			current.copy(
				lines = current.lines.mapIndexed { lineIndex, line ->
					if (lineIndex == index) line.copy(quantity = value) else line
				}
			)
		}
	}

	fun updateLinePrice(index: Int, value: String) {
		_state.update { current ->
			current.copy(
				lines = current.lines.mapIndexed { lineIndex, line ->
					if (lineIndex == index) line.copy(price = value) else line
				}
			)
		}
	}

	fun removeLine(index: Int) {
		_state.update { current ->
			current.copy(lines = current.lines.filterIndexed { lineIndex, _ -> lineIndex != index })
		}
	}

	fun updateInventoryCount(productId: Long, value: String) {
		_state.update { current ->
			current.copy(
				inventoryLines = current.inventoryLines.map { line ->
					if (line.productId == productId) line.copy(counted = value) else line
				}
			)
		}
	}

	fun clearError() {
		_state.update { it.copy(error = null) }
	}

	fun save() {
		val current = _state.value
		if (current.isSaving) return
		val locationId = current.selectedLocationId ?: run {
			_state.update { it.copy(error = "Выберите место хранения") }
			return
		}

		val lines = when (type) {
			DocumentType.INVENTORY -> inventoryInput(current)
			DocumentType.PURCHASE, DocumentType.SALE -> purchaseSaleInput(current)
		} ?: return

		if (lines.isEmpty()) {
			_state.update { it.copy(error = "Добавьте хотя бы один товар") }
			return
		}

		_state.update { it.copy(isSaving = true, error = null) }
		viewModelScope.launch {
			runCatching {
				createDraft(CreateDocumentInput(type = type, locationId = locationId, lines = lines))
			}.onSuccess { documentId ->
				_state.update { it.copy(isSaving = false) }
				_events.emit(DocumentFormEvent.Saved(documentId))
			}.onFailure {
				_state.update { it.copy(isSaving = false, error = "Не удалось сохранить черновик") }
			}
		}
	}

	private fun purchaseSaleInput(current: DocumentFormUiState): List<DraftLineInput>? {
		val result = mutableListOf<DraftLineInput>()
		for (line in current.lines) {
			val quantity = parseUnits(line.quantity)
			val price = parseMoneyToKopecks(line.price)
			if (quantity == null || quantity <= 0) {
				_state.update { it.copy(error = "Количество должно быть положительным: ${line.productName}") }
				return null
			}
			if (price == null || price < 0) {
				_state.update { it.copy(error = "Некорректная цена: ${line.productName}") }
				return null
			}
			if (type == DocumentType.SALE) {
				val available = stockAtLocation(
					stock = current.stock,
					locationId = current.selectedLocationId,
					productId = line.productId
				)
				if (quantity > available) {
					_state.update {
						it.copy(
							error = "Недостаточно товара <${line.productName}>: доступно $available шт."
						)
					}
					return null
				}
			}
			result += DraftLineInput(
				productId = line.productId,
				quantityUnits = quantity,
				priceKopecks = price
			)
		}
		return result
	}

	private fun inventoryInput(current: DocumentFormUiState): List<DraftLineInput>? {
		val result = mutableListOf<DraftLineInput>()
		for (line in current.inventoryLines) {
			val counted = parseUnits(line.counted)
			if (counted == null) {
				_state.update { it.copy(error = "Некорректное количество: ${line.productName}") }
				return null
			}
			result += DraftLineInput(
				productId = line.productId,
				quantityUnits = counted,
				priceKopecks = 0
			)
		}
		return result
	}

	private fun InventoryPrefillRow.toUi(existing: List<InventoryLineUi>): InventoryLineUi {
		val previousCount = existing.firstOrNull { it.productId == productId }?.counted
		return InventoryLineUi(
			productId = productId,
			productName = productName,
			unitsPerBlock = unitsPerBlock,
			systemUnits = systemUnits,
			counted = previousCount ?: systemUnits.toString()
		)
	}
}
