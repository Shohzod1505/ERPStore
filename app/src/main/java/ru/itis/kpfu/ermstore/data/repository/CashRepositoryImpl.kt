package ru.itis.kpfu.ermstore.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.itis.kpfu.ermstore.data.local.dao.CashTransactionDao
import ru.itis.kpfu.ermstore.data.local.entity.CashTransactionEntity
import ru.itis.kpfu.ermstore.data.local.mapper.toDomain
import ru.itis.kpfu.ermstore.domain.model.CashOperationType
import ru.itis.kpfu.ermstore.domain.model.CashTransaction
import ru.itis.kpfu.ermstore.domain.repository.CashRepository
import javax.inject.Inject

class CashRepositoryImpl @Inject constructor(
	private val cashTransactionDao: CashTransactionDao
) : CashRepository {
	override fun observeTransactions(): Flow<List<CashTransaction>> =
		cashTransactionDao.observeAll().map { transactions -> transactions.map { it.toDomain() } }

	override fun observeBalance(): Flow<Long> = cashTransactionDao.observeBalance()

	override suspend fun addManual(
		type: CashOperationType,
		amountKopecks: Long,
		comment: String?
	) {
		require(amountKopecks > 0) { "Amount must be positive" }
		cashTransactionDao.insert(
			CashTransactionEntity(
				type = type,
				amountKopecks = amountKopecks,
				dateMillis = System.currentTimeMillis(),
				comment = comment?.trim()?.ifEmpty { null },
				documentId = null
			)
		)
	}
}
