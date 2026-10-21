package ru.itis.kpfu.ermstore.ui.locations

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
import ru.itis.kpfu.ermstore.domain.usecase.CreateLocationUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveLocationStockUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveLocationsUseCase
import javax.inject.Inject

data class LocationStockLineUi(
	val productName: String,
	val units: Long,
	val unitsPerBlock: Int
)

data class LocationRowUi(
	val id: Long,
	val name: String,
	val parentId: Long?,
	val parentName: String?,
	val stockLines: List<LocationStockLineUi>
)

data class LocationsUiState(
	val rows: List<LocationRowUi> = emptyList(),
	val warehouseOptions: List<Pair<Long, String>> = emptyList(),
	val isLoading: Boolean = true
)

@HiltViewModel
class LocationsViewModel @Inject constructor(
	observeLocations: ObserveLocationsUseCase,
	observeLocationStock: ObserveLocationStockUseCase,
	private val createLocation: CreateLocationUseCase
) : ViewModel() {
	val state: StateFlow<LocationsUiState> = combine(
		observeLocations(),
		observeLocationStock()
	) { locations, stock ->
		val byLocation = stock.groupBy { it.locationId }
		val names = locations.associate { it.id to it.name }
		val warehouses = locations.filter { it.parentId == null }.sortedBy { it.name }
		val warehouseIds = warehouses.map { it.id }.toSet()
		val childrenByParent = locations
			.filter { it.parentId != null }
			.groupBy { it.parentId }
		val orderedLocations = buildList {
			warehouses.forEach { warehouse ->
				add(warehouse)
				addAll(childrenByParent[warehouse.id].orEmpty().sortedBy { it.name })
			}
			addAll(
				locations
					.filter { it.parentId != null && it.parentId !in warehouseIds }
					.sortedBy { it.name }
			)
		}
		val rows = orderedLocations
			.map { location ->
				LocationRowUi(
					id = location.id,
					name = location.name,
					parentId = location.parentId,
					parentName = location.parentId?.let { names[it] },
					stockLines = byLocation[location.id].orEmpty().map { line ->
						LocationStockLineUi(
							productName = line.productName,
							units = line.units,
							unitsPerBlock = line.unitsPerBlock
						)
					}
				)
			}
		LocationsUiState(
			rows = rows,
			warehouseOptions = locations
				.filter { it.parentId == null }
				.sortedBy { it.name }
				.map { it.id to it.name },
			isLoading = false
		)
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = LocationsUiState()
	)

	private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
	val events = _events.asSharedFlow()

	fun create(name: String, parentId: Long?) {
		viewModelScope.launch {
			runCatching {
				createLocation(name, parentId)
			}.onSuccess {
				_events.emit(if (parentId == null) "Склад создан" else "Подсклад создан")
			}.onFailure {
				_events.emit("Не удалось создать место хранения")
			}
		}
	}
}
