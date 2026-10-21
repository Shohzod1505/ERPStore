package ru.itis.kpfu.ermstore.data.local

import androidx.room.TypeConverter
import ru.itis.kpfu.ermstore.data.local.entity.StockMovementType
import ru.itis.kpfu.ermstore.domain.model.CashOperationType
import ru.itis.kpfu.ermstore.domain.model.DocumentStatus
import ru.itis.kpfu.ermstore.domain.model.DocumentType

class Converters {
	@TypeConverter
	fun fromDocumentType(value: DocumentType): String = value.name

	@TypeConverter
	fun toDocumentType(value: String): DocumentType = DocumentType.valueOf(value)

	@TypeConverter
	fun fromDocumentStatus(value: DocumentStatus): String = value.name

	@TypeConverter
	fun toDocumentStatus(value: String): DocumentStatus = DocumentStatus.valueOf(value)

	@TypeConverter
	fun fromCashOperationType(value: CashOperationType): String = value.name

	@TypeConverter
	fun toCashOperationType(value: String): CashOperationType = CashOperationType.valueOf(value)

	@TypeConverter
	fun fromStockMovementType(value: StockMovementType): String = value.name

	@TypeConverter
	fun toStockMovementType(value: String): StockMovementType = StockMovementType.valueOf(value)
}
