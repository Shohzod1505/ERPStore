package ru.itis.kpfu.ermstore.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.itis.kpfu.ermstore.domain.model.LocationStockRow
import ru.itis.kpfu.ermstore.domain.model.Product
import ru.itis.kpfu.ermstore.domain.model.StorageLocation

class LocationAvailabilityTest {
	private val warehouseA = StorageLocation(1, "A", null)
	private val warehouseB = StorageLocation(5, "B", null)
	private val fridge = StorageLocation(2, "Fridge", 1)
	private val main = StorageLocation(3, "Main", 1)
	private val room = StorageLocation(4, "Main", 5)
	private val cola = Product(10, "Cola", 12)
	private val water = Product(11, "Water", 6)

	@Test
	fun `warehouses are followed by their subwarehouses`() {
		val ordered = orderedLocations(listOf(room, fridge, warehouseB, main, warehouseA))

		assertEquals(listOf(1L, 2L, 3L, 5L, 4L), ordered.map { it.id })
	}

	@Test
	fun `sale locations require all selected products`() {
		val stock = listOf(
			stockRow(location = fridge, product = cola, units = 5),
			stockRow(location = main, product = cola, units = 5),
			stockRow(location = main, product = water, units = 5)
		)

		val locations = saleLocations(
			locations = listOf(warehouseA, fridge, main),
			stock = stock,
			productIds = setOf(cola.id, water.id)
		)

		assertEquals(listOf(main.id), locations.map { it.id })
	}

	@Test
	fun `sale products are limited to stock at the selected location`() {
		val stock = listOf(
			stockRow(location = fridge, product = cola, units = 5),
			stockRow(location = main, product = cola, units = 5),
			stockRow(location = main, product = water, units = 5)
		)

		val products = saleProducts(
			products = listOf(cola, water),
			stock = stock,
			locationId = fridge.id
		)

		assertEquals(listOf(cola.id), products.map { it.id })
	}

	private fun stockRow(
		location: StorageLocation,
		product: Product,
		units: Long
	): LocationStockRow = LocationStockRow(
		locationId = location.id,
		locationName = location.name,
		parentId = location.parentId,
		parentName = warehouseA.name.takeIf { location.parentId == warehouseA.id },
		productId = product.id,
		productName = product.name,
		unitsPerBlock = product.unitsPerBlock,
		units = units
	)
}
