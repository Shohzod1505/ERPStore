package ru.itis.kpfu.ermstore.ui.util

import ru.itis.kpfu.ermstore.domain.model.LocationStockRow
import ru.itis.kpfu.ermstore.domain.model.Product
import ru.itis.kpfu.ermstore.domain.model.StorageLocation

fun operationLocations(locations: List<StorageLocation>): List<StorageLocation> =
	orderedLocations(locations).filter { it.parentId != null }

fun saleLocations(
	locations: List<StorageLocation>,
	stock: List<LocationStockRow>,
	productIds: Set<Long>
): List<StorageLocation> {
	val leaves = operationLocations(locations)
	if (productIds.isEmpty()) return leaves
	return leaves.filter { location ->
		productIds.all { productId ->
			stock.any { row ->
				row.locationId == location.id && row.productId == productId && row.units > 0
			}
		}
	}
}

fun saleProducts(
	products: List<Product>,
	stock: List<LocationStockRow>,
	locationId: Long?
): List<Product> {
	if (locationId == null) return products
	return products.filter { product ->
		stock.any { row ->
			row.locationId == locationId && row.productId == product.id && row.units > 0
		}
	}
}

fun stockAtLocation(
	stock: List<LocationStockRow>,
	locationId: Long?,
	productId: Long
): Long = stock.firstOrNull {
	it.locationId == locationId && it.productId == productId
}?.units ?: 0
