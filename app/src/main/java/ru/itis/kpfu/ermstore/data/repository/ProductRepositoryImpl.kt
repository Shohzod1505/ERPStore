package ru.itis.kpfu.ermstore.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.itis.kpfu.ermstore.data.local.dao.ProductDao
import ru.itis.kpfu.ermstore.data.local.entity.ProductEntity
import ru.itis.kpfu.ermstore.data.local.mapper.toDomain
import ru.itis.kpfu.ermstore.domain.model.Product
import ru.itis.kpfu.ermstore.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
	private val productDao: ProductDao
) : ProductRepository {
	override fun observeProducts(): Flow<List<Product>> =
		productDao.observeAll().map { products -> products.map { it.toDomain() } }

	override suspend fun getProduct(id: Long): Product? =
		productDao.getById(id)?.toDomain()

	override suspend fun createProduct(name: String, unitsPerBlock: Int): Long {
		val trimmedName = name.trim()
		require(trimmedName.isNotEmpty()) { "Product name must not be blank" }
		require(unitsPerBlock > 0) { "Units per block must be positive" }
		return productDao.insert(
			ProductEntity(
				name = trimmedName,
				unitsPerBlock = unitsPerBlock
			)
		)
	}
}
