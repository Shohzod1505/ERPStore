package ru.itis.kpfu.ermstore.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.itis.kpfu.ermstore.data.local.ErmStoreDatabase
import ru.itis.kpfu.ermstore.data.local.dao.CashTransactionDao
import ru.itis.kpfu.ermstore.data.local.dao.DocumentDao
import ru.itis.kpfu.ermstore.data.local.dao.DocumentLineDao
import ru.itis.kpfu.ermstore.data.local.dao.LocationDao
import ru.itis.kpfu.ermstore.data.local.dao.ProductDao
import ru.itis.kpfu.ermstore.data.local.dao.StockMovementDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
	@Provides
	@Singleton
	fun provideDatabase(@ApplicationContext context: Context): ErmStoreDatabase =
		Room.databaseBuilder(
			context,
			ErmStoreDatabase::class.java,
			"ermstore.db"
		)
			.fallbackToDestructiveMigration(dropAllTables = true)
			.build()

	@Provides
	fun provideProductDao(database: ErmStoreDatabase): ProductDao = database.productDao()

	@Provides
	fun provideLocationDao(database: ErmStoreDatabase): LocationDao = database.locationDao()

	@Provides
	fun provideDocumentDao(database: ErmStoreDatabase): DocumentDao = database.documentDao()

	@Provides
	fun provideDocumentLineDao(database: ErmStoreDatabase): DocumentLineDao = database.documentLineDao()

	@Provides
	fun provideStockMovementDao(database: ErmStoreDatabase): StockMovementDao = database.stockMovementDao()

	@Provides
	fun provideCashTransactionDao(database: ErmStoreDatabase): CashTransactionDao = database.cashTransactionDao()
}
