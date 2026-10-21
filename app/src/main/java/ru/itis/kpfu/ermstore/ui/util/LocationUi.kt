package ru.itis.kpfu.ermstore.ui.util

import ru.itis.kpfu.ermstore.domain.model.StorageLocation

fun orderedLocations(locations: List<StorageLocation>): List<StorageLocation> {
	val warehouses = locations.filter { it.parentId == null }.sortedBy { it.name }
	val warehouseIds = warehouses.map { it.id }.toSet()
	val childrenByParent = locations
		.filter { it.parentId != null }
		.groupBy { it.parentId }
	return buildList {
		warehouses.forEach { warehouse ->
			add(warehouse)
			addAll(childrenByParent[warehouse.id].orEmpty().sortedBy { it.name })
		}
		addAll(
			locations
				.filter { it.parentId != null && it.parentId !in warehouseIds }
				.sortedBy { it.name }
		)
	}
}

fun locationDisplayName(location: StorageLocation, names: Map<Long, String>): String =
	if (location.parentId == null) {
		location.name
	} else {
		"${names[location.parentId] ?: "Склад"} / ${location.name}"
	}
