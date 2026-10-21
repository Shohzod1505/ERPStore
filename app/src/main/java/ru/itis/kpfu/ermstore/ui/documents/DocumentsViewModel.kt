package ru.itis.kpfu.ermstore.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import ru.itis.kpfu.ermstore.domain.model.Document
import ru.itis.kpfu.ermstore.domain.model.DocumentStatus
import ru.itis.kpfu.ermstore.domain.model.DocumentType
import ru.itis.kpfu.ermstore.domain.usecase.ObserveDocumentsUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveLocationsUseCase
import javax.inject.Inject

data class DocumentRowUi(
	val id: Long,
	val type: DocumentType,
	val status: DocumentStatus,
	val number: String,
	val dateMillis: Long,
	val locationName: String,
	val totalKopecks: Long
)

data class DocumentsUiState(
	val type: DocumentType = DocumentType.PURCHASE,
	val rows: List<DocumentRowUi> = emptyList(),
	val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DocumentsViewModel @Inject constructor(
	observeDocuments: ObserveDocumentsUseCase,
	observeLocations: ObserveLocationsUseCase
) : ViewModel() {
	private val selectedType = MutableStateFlow(DocumentType.PURCHASE)

	val state: StateFlow<DocumentsUiState> = combine(
		selectedType.flatMapLatest { type -> observeDocuments(type) },
		observeLocations(),
		selectedType
	) { documents, locations, type ->
		val locationNames = locations.associate { it.id to it.name }
		DocumentsUiState(
			type = type,
			rows = documents.map { it.toRowUi(locationNames) },
			isLoading = false
		)
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = DocumentsUiState()
	)

	fun selectType(type: DocumentType) {
		selectedType.value = type
	}

	private fun Document.toRowUi(locationNames: Map<Long, String>): DocumentRowUi = DocumentRowUi(
		id = id,
		type = type,
		status = status,
		number = number,
		dateMillis = dateMillis,
		locationName = locationNames[locationId] ?: "—",
		totalKopecks = totalKopecks
	)
}
