package ru.itis.kpfu.ermstore.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.itis.kpfu.ermstore.data.local.ErmStoreDatabase
import ru.itis.kpfu.ermstore.data.local.dao.CashTransactionDao
import ru.itis.kpfu.ermstore.data.local.entity.CashTransactionEntity
import ru.itis.kpfu.ermstore.data.local.mapper.toDomain
import ru.itis.kpfu.ermstore.domain.calc.CashMath
import ru.itis.kpfu.ermstore.domain.model.CashOperationError
import ru.itis.kpfu.ermstore.domain.model.CashOperationResult
import ru.itis.kpfu.ermstore.domain.model.CashOperationType
import ru.itis.kpfu.ermstore.domain.model.CashTransaction
import ru.itis.kpfu.ermstore.domain.repository.CashRepository
import javax.inject.Inject

class CashRepositoryImpl @Inject constructor(
	private val database: ErmStoreDatabase,
	private val cashTransactionDao: CashTransactionDao
) : CashRepository {
	override fun observeTransactions(): Flow<List<CashTransaction>> =
		cashTransactionDao.observeAll().map { transactions -> transactions.map { it.toDomain() } }

	override fun observeBalance(): Flow<Long> = cashTransactionDao.observeBalance()

	override suspend fun addManual(
		type: CashOperationType,
		amountKopecks: Long,
		comment: String?
	): CashOperationResult {
		if (amountKopecks <= 0) {
			return CashOperationResult.Failure(CashOperationError.InvalidAmount)
		}
		return database.withTransaction {
			val balance = cashTransactionDao.getBalance()
			if (type.cashSign < 0 && !CashMath.canSpend(balance, amountKopecks)) {
				return@withTransaction CashOperationResult.Failure(
					CashOperationError.InsufficientFunds(
						availableKopecks = balance,
						requestedKopecks = amountKopecks
					)
				)
			}
			cashTransactionDao.insert(
				CashTransactionEntity(
					type = type,
					amountKopecks = amountKopecks,
					dateMillis = System.currentTimeMillis(),
					comment = comment?.trim()?.ifEmpty { null },
					documentId = null
				)
			)
			CashOperationResult.Success
		}
	}
}
