package ru.itis.kpfu.ermstore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.itis.kpfu.ermstore.data.local.entity.DocumentEntity
import ru.itis.kpfu.ermstore.data.local.model.SalesReportRowDb
import ru.itis.kpfu.ermstore.domain.model.DocumentStatus
import ru.itis.kpfu.ermstore.domain.model.DocumentType

@Dao
interface DocumentDao {
	@Query("SELECT * FROM documents WHERE type = :type ORDER BY dateMillis DESC, id DESC")
	fun observeByType(type: DocumentType): Flow<List<DocumentEntity>>

	@Query("SELECT * FROM documents WHERE id = :id")
	suspend fun getById(id: Long): DocumentEntity?

	@Insert
	suspend fun insert(document: DocumentEntity): Long

	@Query("SELECT COUNT(*) FROM documents WHERE type = :type")
	suspend fun countByType(type: DocumentType): Int

	@Query("UPDATE documents SET status = :status, totalKopecks = :totalKopecks WHERE id = :id")
	suspend fun updateStatusAndTotal(
		id: Long,
		status: DocumentStatus,
		totalKopecks: Long
	)

	@Query("DELETE FROM documents WHERE id = :id AND status = 'DRAFT'")
	suspend fun deleteDraft(id: Long): Int

	@Query(
		"""
		SELECT COALESCE(SUM(totalKopecks), 0) FROM documents
		WHERE type = 'SALE' AND status = 'POSTED'
		"""
	)
	fun observePostedSalesTotal(): Flow<Long>

	@Query(
		"""
		SELECT d.id AS documentId, d.number AS number, d.dateMillis AS dateMillis,
		       l.name AS locationName, d.totalKopecks AS totalKopecks
		FROM documents d
		INNER JOIN locations l ON l.id = d.locationId
		WHERE d.type = 'SALE' AND d.status = 'POSTED'
		ORDER BY d.dateMillis DESC, d.id DESC
		"""
	)
	fun observeSalesReportRows(): Flow<List<SalesReportRowDb>>
}
