package ru.itis.kpfu.ermstore.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.itis.kpfu.ermstore.domain.model.StorageLocation
import ru.itis.kpfu.ermstore.domain.repository.LocationRepository
import javax.inject.Inject

class ObserveLocationsUseCase @Inject constructor(
	private val repository: LocationRepository
) {
	operator fun invoke(): Flow<List<StorageLocation>> = repository.observeLocations()
}

class CreateLocationUseCase @Inject constructor(
	private val repository: LocationRepository
) {
	suspend operator fun invoke(name: String, parentId: Long?): Long =
		repository.createLocation(name, parentId)
}
