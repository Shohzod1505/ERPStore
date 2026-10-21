package ru.itis.kpfu.ermstore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.itis.kpfu.ermstore.data.local.entity.DocumentLineEntity
import ru.itis.kpfu.ermstore.data.local.model.DocumentLineRow
import ru.itis.kpfu.ermstore.data.local.model.InventoryAdjustmentTotals

@Dao
interface DocumentLineDao {
	@Insert
	suspend fun insertAll(lines: List<DocumentLineEntity>)

	@Query(
		"""
		SELECT l.id AS id, l.documentId AS documentId, l.productId AS productId,
		       p.name AS productName, l.quantityUnits AS quantityUnits,
		       l.priceKopecks AS priceKopecks, l.costKopecks AS costKopecks
		FROM document_lines l
		INNER JOIN products p ON p.id = l.productId
		WHERE l.documentId = :documentId
		ORDER BY l.id
		"""
	)
	suspend fun getRows(documentId: Long): List<DocumentLineRow>

	@Query("UPDATE document_lines SET costKopecks = :costKopecks WHERE id = :id")
	suspend fun updateCost(id: Long, costKopecks: Long)

	@Query(
		"""
		SELECT COALESCE(SUM(l.costKopecks), 0)
		FROM document_lines l
		INNER JOIN documents d ON d.id = l.documentId
		WHERE d.type = 'SALE' AND d.status = 'POSTED'
		"""
	)
	fun observeCogs(): Flow<Long>

	@Query(
		"""
		SELECT
			COALESCE(SUM(CASE WHEN l.costKopecks > 0 THEN l.costKopecks ELSE 0 END), 0) AS surplusKopecks,
			COALESCE(SUM(CASE WHEN l.costKopecks < 0 THEN -l.costKopecks ELSE 0 END), 0) AS shortageKopecks
		FROM document_lines l
		INNER JOIN documents d ON d.id = l.documentId
		WHERE d.type = 'INVENTORY' AND d.status = 'POSTED'
		"""
	)
	fun observeInventoryAdjustments(): Flow<InventoryAdjustmentTotals>
}
