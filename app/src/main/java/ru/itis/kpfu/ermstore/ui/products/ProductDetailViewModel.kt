package ru.itis.kpfu.ermstore.ui.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.itis.kpfu.ermstore.domain.model.LocationStockRow
import ru.itis.kpfu.ermstore.domain.model.Product
import ru.itis.kpfu.ermstore.domain.model.ProductMovement
import ru.itis.kpfu.ermstore.domain.usecase.ObserveProductDetailUseCase
import javax.inject.Inject

data class ProductDetailUiState(
	val product: Product? = null,
	val stockByLocation: List<LocationStockRow> = emptyList(),
	val movements: List<ProductMovement> = emptyList(),
	val isLoading: Boolean = true
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	observeProductDetail: ObserveProductDetailUseCase
) : ViewModel() {
	private val productId = savedStateHandle.get<Long>("productId") ?: 0L

	val state: StateFlow<ProductDetailUiState> = observeProductDetail(productId)
		.map { detail ->
			ProductDetailUiState(
				product = detail?.product,
				stockByLocation = detail?.stockByLocation.orEmpty(),
				movements = detail?.movements.orEmpty(),
				isLoading = false
			)
		}
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = ProductDetailUiState()
		)
}
