package ru.itis.kpfu.ermstore.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.itis.kpfu.ermstore.domain.usecase.CreateProductUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveProductStockUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveProductsUseCase
import javax.inject.Inject

data class ProductRowUi(
	val id: Long,
	val name: String,
	val unitsPerBlock: Int,
	val units: Long
) {
	val blocks: Long get() = units / unitsPerBlock
	val remainderUnits: Long get() = units % unitsPerBlock
}

data class ProductsUiState(
	val rows: List<ProductRowUi> = emptyList(),
	val isLoading: Boolean = true
)

@HiltViewModel
class ProductsViewModel @Inject constructor(
	observeProducts: ObserveProductsUseCase,
	observeProductStock: ObserveProductStockUseCase,
	private val createProduct: CreateProductUseCase
) : ViewModel() {
	val state: StateFlow<ProductsUiState> = combine(
		observeProducts(),
		observeProductStock()
	) { products, stock ->
		val stockByProduct = stock.associateBy { it.productId }
		ProductsUiState(
			rows = products.map { product ->
				ProductRowUi(
					id = product.id,
					name = product.name,
					unitsPerBlock = product.unitsPerBlock,
					units = stockByProduct[product.id]?.units ?: 0
				)
			},
			isLoading = false
		)
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = ProductsUiState()
	)

	private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
	val events = _events.asSharedFlow()

	fun create(name: String, unitsPerBlock: Int) {
		viewModelScope.launch {
			runCatching {
				createProduct(name, unitsPerBlock)
			}.onSuccess {
				_events.emit("Товар добавлен")
			}.onFailure {
				_events.emit("Не удалось добавить товар")
			}
		}
	}
}
