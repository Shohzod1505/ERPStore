package ru.itis.kpfu.ermstore.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.itis.kpfu.ermstore.data.repository.CashRepositoryImpl
import ru.itis.kpfu.ermstore.data.repository.DocumentRepositoryImpl
import ru.itis.kpfu.ermstore.data.repository.LocationRepositoryImpl
import ru.itis.kpfu.ermstore.data.repository.ProductRepositoryImpl
import ru.itis.kpfu.ermstore.data.repository.ReportRepositoryImpl
import ru.itis.kpfu.ermstore.domain.repository.CashRepository
import ru.itis.kpfu.ermstore.domain.repository.DocumentRepository
import ru.itis.kpfu.ermstore.domain.repository.LocationRepository
import ru.itis.kpfu.ermstore.domain.repository.ProductRepository
import ru.itis.kpfu.ermstore.domain.repository.ReportRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
	@Binds
	@Singleton
	abstract fun bindProductRepository(implementation: ProductRepositoryImpl): ProductRepository

	@Binds
	@Singleton
	abstract fun bindLocationRepository(implementation: LocationRepositoryImpl): LocationRepository

	@Binds
	@Singleton
	abstract fun bindDocumentRepository(implementation: DocumentRepositoryImpl): DocumentRepository

	@Binds
	@Singleton
	abstract fun bindCashRepository(implementation: CashRepositoryImpl): CashRepository

	@Binds
	@Singleton
	abstract fun bindReportRepository(implementation: ReportRepositoryImpl): ReportRepository
}
