package ru.itis.kpfu.ermstore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.itis.kpfu.ermstore.data.local.entity.StockMovementEntity
import ru.itis.kpfu.ermstore.data.local.model.InventoryPrefillRowDb
import ru.itis.kpfu.ermstore.data.local.model.LocationStockRowDb
import ru.itis.kpfu.ermstore.data.local.model.ProductMovementRowDb

@Dao
interface StockMovementDao {
	@Insert
	suspend fun insert(movement: StockMovementEntity)

	@Query("SELECT * FROM stock_movements WHERE documentId = :documentId")
	suspend fun getForDocument(documentId: Long): List<StockMovementEntity>

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
		       parent.name AS parentName, p.id AS productId, p.name AS productName,
		       p.unitsPerBlock AS unitsPerBlock, COALESCE(SUM(m.quantityDelta), 0) AS units
		FROM locations l
		CROSS JOIN products p
		LEFT JOIN stock_movements m ON m.locationId = l.id AND m.productId = p.id
		LEFT JOIN locations parent ON parent.id = l.parentId
		GROUP BY l.id, p.id
		HAVING COALESCE(SUM(m.quantityDelta), 0) != 0
		ORDER BY COALESCE(parent.name, l.name), l.name, p.name
		"""
	)
	fun observeLocationStock(): Flow<List<LocationStockRowDb>>

	@Query(
		"""
		SELECT l.id AS locationId, l.name AS locationName, l.parentId AS parentId,
		       parent.name AS parentName, p.id AS productId, p.name AS productName,
		       p.unitsPerBlock AS unitsPerBlock, COALESCE(SUM(m.quantityDelta), 0) AS units
		FROM products p
		INNER JOIN stock_movements m ON m.productId = p.id
		INNER JOIN locations l ON l.id = m.locationId
		LEFT JOIN locations parent ON parent.id = l.parentId
		WHERE p.id = :productId
		GROUP BY l.id, p.id
		HAVING COALESCE(SUM(m.quantityDelta), 0) != 0
		ORDER BY COALESCE(parent.name, l.name), l.name
		"""
	)
	fun observeProductLocationStock(productId: Long): Flow<List<LocationStockRowDb>>

	@Query(
		"""
		SELECT m.id AS id, m.dateMillis AS dateMillis, m.quantityDelta AS quantityDelta,
		       m.unitCostKopecks AS unitCostKopecks, d.id AS documentId,
		       d.number AS documentNumber, d.type AS documentType,
		       l.name AS locationName, parent.name AS parentName
		FROM stock_movements m
		INNER JOIN documents d ON d.id = m.documentId
		INNER JOIN locations l ON l.id = m.locationId
		LEFT JOIN locations parent ON parent.id = l.parentId
		WHERE m.productId = :productId
		ORDER BY m.dateMillis DESC, m.id DESC
		"""
	)
	fun observeProductMovements(productId: Long): Flow<List<ProductMovementRowDb>>

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
