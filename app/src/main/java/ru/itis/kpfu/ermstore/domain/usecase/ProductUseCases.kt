package ru.itis.kpfu.ermstore.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.itis.kpfu.ermstore.domain.model.Product
import ru.itis.kpfu.ermstore.domain.repository.ProductRepository
import javax.inject.Inject

class ObserveProductsUseCase @Inject constructor(
	private val repository: ProductRepository
) {
	operator fun invoke(): Flow<List<Product>> = repository.observeProducts()
}

class CreateProductUseCase @Inject constructor(
	private val repository: ProductRepository
) {
	suspend operator fun invoke(name: String, unitsPerBlock: Int): Long =
		repository.createProduct(name, unitsPerBlock)
}
