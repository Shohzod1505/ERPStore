package ru.itis.kpfu.ermstore.ui.documents

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.kpfu.ermstore.domain.model.DocumentWithLines
import ru.itis.kpfu.ermstore.domain.model.PostingResult
import ru.itis.kpfu.ermstore.domain.usecase.DeleteDraftUseCase
import ru.itis.kpfu.ermstore.domain.usecase.GetDocumentUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveLocationsUseCase
import ru.itis.kpfu.ermstore.domain.usecase.PostDocumentUseCase
import ru.itis.kpfu.ermstore.ui.util.postingErrorText
import javax.inject.Inject

data class DocumentDetailUiState(
	val document: DocumentWithLines? = null,
	val locationName: String = "",
	val isLoading: Boolean = true,
	val isWorking: Boolean = false
)

sealed interface DocumentDetailEvent {
	data class Message(val text: String) : DocumentDetailEvent
	data object Deleted : DocumentDetailEvent
}

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	private val getDocument: GetDocumentUseCase,
	private val postDocument: PostDocumentUseCase,
	private val deleteDraft: DeleteDraftUseCase,
	observeLocations: ObserveLocationsUseCase
) : ViewModel() {
	private val documentId = savedStateHandle.get<Long>("documentId") ?: 0L
	private var locationNames: Map<Long, String> = emptyMap()

	private val _state = MutableStateFlow(DocumentDetailUiState())
	val state: StateFlow<DocumentDetailUiState> = _state.asStateFlow()

	private val _events = MutableSharedFlow<DocumentDetailEvent>(extraBufferCapacity = 1)
	val events = _events.asSharedFlow()

	init {
		viewModelScope.launch {
			observeLocations().collect { locations ->
				locationNames = locations.associate { it.id to it.name }
				_state.update { current ->
					val locationId = current.document?.document?.locationId
					current.copy(locationName = locationId?.let { locationNames[it] } ?: current.locationName)
				}
			}
		}
		viewModelScope.launch {
			loadDocument()
		}
	}

	fun post() {
		if (_state.value.isWorking) return
		_state.update { it.copy(isWorking = true) }
		viewModelScope.launch {
			when (val result = postDocument(documentId)) {
				PostingResult.Success -> {
					loadDocument()
					_events.emit(DocumentDetailEvent.Message("Документ проведён"))
				}
				is PostingResult.Failure -> {
					_events.emit(DocumentDetailEvent.Message(postingErrorText(result.error)))
				}
			}
			_state.update { it.copy(isWorking = false) }
		}
	}

	fun delete() {
		if (_state.value.isWorking) return
		_state.update { it.copy(isWorking = true) }
		viewModelScope.launch {
			runCatching {
				deleteDraft(documentId)
			}.onSuccess {
				_events.emit(DocumentDetailEvent.Deleted)
			}.onFailure {
				_events.emit(DocumentDetailEvent.Message("Не удалось удалить черновик"))
			}
			_state.update { it.copy(isWorking = false) }
		}
	}

	private suspend fun loadDocument() {
		val document = getDocument(documentId)
		_state.update {
			it.copy(
				document = document,
				locationName = document?.document?.locationId?.let { id -> locationNames[id] } ?: "",
				isLoading = false
			)
		}
	}
}
