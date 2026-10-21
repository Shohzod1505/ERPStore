package ru.itis.kpfu.ermstore.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.itis.kpfu.ermstore.data.local.dao.LocationDao
import ru.itis.kpfu.ermstore.data.local.entity.LocationEntity
import ru.itis.kpfu.ermstore.data.local.mapper.toDomain
import ru.itis.kpfu.ermstore.domain.model.StorageLocation
import ru.itis.kpfu.ermstore.domain.repository.LocationRepository
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
	private val locationDao: LocationDao
) : LocationRepository {
	override fun observeLocations(): Flow<List<StorageLocation>> =
		locationDao.observeAll().map { locations -> locations.map { it.toDomain() } }

	override suspend fun createLocation(name: String, parentId: Long?): Long {
		val trimmedName = name.trim()
		require(trimmedName.isNotEmpty()) { "Location name must not be blank" }
		if (parentId != null) {
			val parent = requireNotNull(locationDao.getById(parentId)) {
				"Parent location does not exist"
			}
			require(parent.parentId == null) {
				"Only one level of subwarehouses is supported"
			}
		}
		return locationDao.insert(
			LocationEntity(
				name = trimmedName,
				parentId = parentId
			)
		)
	}
}
