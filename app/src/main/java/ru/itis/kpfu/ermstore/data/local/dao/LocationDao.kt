package ru.itis.kpfu.ermstore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.itis.kpfu.ermstore.data.local.entity.LocationEntity

@Dao
interface LocationDao {
	@Query(
		"""
		SELECT * FROM locations
		ORDER BY CASE WHEN parentId IS NULL THEN 0 ELSE 1 END, name
		"""
	)
	fun observeAll(): Flow<List<LocationEntity>>

	@Query("SELECT * FROM locations WHERE id = :id")
	suspend fun getById(id: Long): LocationEntity?

	@Insert
	suspend fun insert(location: LocationEntity): Long

	@Query("SELECT COUNT(*) FROM locations")
	suspend fun count(): Int
}
