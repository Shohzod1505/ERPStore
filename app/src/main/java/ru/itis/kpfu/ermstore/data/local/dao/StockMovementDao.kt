package ru.itis.kpfu.ermstore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.itis.kpfu.ermstore.data.local.entity.StockMovementEntity
import ru.itis.kpfu.ermstore.data.local.model.InventoryPrefillRowDb
import ru.itis.kpfu.ermstore.data.local.model.LocationStockRowDb

@Dao
interface StockMovementDao {
	@Insert
	suspend fun insert(movement: StockMovementEntity)

	@Query(
		"""
		SELECT COALESCE(SUM(quantityDelta), 0) FROM stock_movements
		WHERE productId = :productId AND locationId = :locationId
		"""
	)
	suspend fun getStockAtLocation(productId: Long, locationId: Long): Long

	@Query(
		"""
		SELECT COALESCE(SUM(quantityDelta), 0) FROM stock_movements
		WHERE productId = :productId
		"""
	)
	suspend fun getTotalStock(productId: Long): Long

	@Query(
		"""
		SELECT l.id AS locationId, l.name AS locationName, l.parentId AS parentId,
		       p.id AS productId, p.name AS productName, p.unitsPerBlock AS unitsPerBlock,
		       COALESCE(SUM(m.quantityDelta), 0) AS units
		FROM locations l
		CROSS JOIN products p
		LEFT JOIN stock_movements m ON m.locationId = l.id AND m.productId = p.id
		GROUP BY l.id, p.id
		HAVING COALESCE(SUM(m.quantityDelta), 0) != 0
		ORDER BY l.name, p.name
		"""
	)
	fun observeLocationStock(): Flow<List<LocationStockRowDb>>

	@Query(
		"""
		SELECT p.id AS productId, p.name AS productName, p.unitsPerBlock AS unitsPerBlock,
		       COALESCE(SUM(m.quantityDelta), 0) AS systemUnits
		FROM products p
		LEFT JOIN stock_movements m ON m.productId = p.id AND m.locationId = :locationId
		GROUP BY p.id
		ORDER BY p.name
		"""
	)
	fun observeInventoryPrefill(locationId: Long): Flow<List<InventoryPrefillRowDb>>
}
