package ru.itis.kpfu.ermstore.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.itis.kpfu.ermstore.data.local.dao.CashTransactionDao
import ru.itis.kpfu.ermstore.data.local.dao.DocumentDao
import ru.itis.kpfu.ermstore.data.local.dao.DocumentLineDao
import ru.itis.kpfu.ermstore.data.local.dao.LocationDao
import ru.itis.kpfu.ermstore.data.local.dao.ProductDao
import ru.itis.kpfu.ermstore.data.local.dao.StockMovementDao
import ru.itis.kpfu.ermstore.data.local.entity.CashTransactionEntity
import ru.itis.kpfu.ermstore.data.local.entity.DocumentEntity
import ru.itis.kpfu.ermstore.data.local.entity.DocumentLineEntity
import ru.itis.kpfu.ermstore.data.local.entity.LocationEntity
import ru.itis.kpfu.ermstore.data.local.entity.ProductEntity
import ru.itis.kpfu.ermstore.data.local.entity.StockMovementEntity

@Database(
	entities = [
		ProductEntity::class,
		LocationEntity::class,
		DocumentEntity::class,
		DocumentLineEntity::class,
		StockMovementEntity::class,
		CashTransactionEntity::class
	],
	version = 2,
	exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ErmStoreDatabase : RoomDatabase() {
	abstract fun productDao(): ProductDao
	abstract fun locationDao(): LocationDao
	abstract fun documentDao(): DocumentDao
	abstract fun documentLineDao(): DocumentLineDao
	abstract fun stockMovementDao(): StockMovementDao
	abstract fun cashTransactionDao(): CashTransactionDao
}
