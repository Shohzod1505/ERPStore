package ru.itis.kpfu.ermstore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.itis.kpfu.ermstore.data.local.entity.ProductEntity
import ru.itis.kpfu.ermstore.data.local.model.ProductStockRow

@Dao
interface ProductDao {
	@Query("SELECT * FROM products ORDER BY name")
	fun observeAll(): Flow<List<ProductEntity>>

	@Query("SELECT * FROM products WHERE id = :id")
	suspend fun getById(id: Long): ProductEntity?

	@Insert
	suspend fun insert(product: ProductEntity): Long

	@Query("UPDATE products SET averageCostKopecks = :costKopecks WHERE id = :id")
	suspend fun updateAverageCost(id: Long, costKopecks: Long)

	@Query("SELECT COUNT(*) FROM products")
	suspend fun count(): Int

	@Query(
		"""
		SELECT p.id AS productId, p.name AS productName, p.unitsPerBlock AS unitsPerBlock,
		       COALESCE(SUM(m.quantityDelta), 0) AS units
		FROM products p
		LEFT JOIN stock_movements m ON m.productId = p.id
		GROUP BY p.id
		ORDER BY p.name
		"""
	)
	fun observeProductStock(): Flow<List<ProductStockRow>>
}
