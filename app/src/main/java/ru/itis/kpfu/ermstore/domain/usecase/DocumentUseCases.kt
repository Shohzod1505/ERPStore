package ru.itis.kpfu.ermstore.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.itis.kpfu.ermstore.domain.model.CreateDocumentInput
import ru.itis.kpfu.ermstore.domain.model.Document
import ru.itis.kpfu.ermstore.domain.model.DocumentType
import ru.itis.kpfu.ermstore.domain.model.DocumentWithLines
import ru.itis.kpfu.ermstore.domain.model.InventoryPrefillRow
import ru.itis.kpfu.ermstore.domain.model.PostingResult
import ru.itis.kpfu.ermstore.domain.repository.DocumentRepository
import javax.inject.Inject

class ObserveDocumentsUseCase @Inject constructor(
	private val repository: DocumentRepository
) {
	operator fun invoke(type: DocumentType): Flow<List<Document>> =
		repository.observeDocuments(type)
}

class GetDocumentUseCase @Inject constructor(
	private val repository: DocumentRepository
) {
	suspend operator fun invoke(id: Long): DocumentWithLines? = repository.getDocument(id)
}

class CreateDraftUseCase @Inject constructor(
	private val repository: DocumentRepository
) {
	suspend operator fun invoke(input: CreateDocumentInput): Long = repository.createDraft(input)
}

class PostDocumentUseCase @Inject constructor(
	private val repository: DocumentRepository
) {
	suspend operator fun invoke(id: Long): PostingResult = repository.postDocument(id)
}

class DeleteDraftUseCase @Inject constructor(
	private val repository: DocumentRepository
) {
	suspend operator fun invoke(id: Long) = repository.deleteDraft(id)
}

class ObserveInventoryPrefillUseCase @Inject constructor(
	private val repository: DocumentRepository
) {
	operator fun invoke(locationId: Long): Flow<List<InventoryPrefillRow>> =
		repository.observeInventoryPrefill(locationId)
}
