package ru.itis.kpfu.ermstore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.itis.kpfu.ermstore.data.local.entity.CashTransactionEntity

@Dao
interface CashTransactionDao {
	@Query("SELECT * FROM cash_transactions ORDER BY dateMillis DESC, id DESC")
	fun observeAll(): Flow<List<CashTransactionEntity>>

	@Insert
	suspend fun insert(transaction: CashTransactionEntity): Long

	@Query(
		"""
		SELECT COALESCE(SUM(
			CASE
				WHEN type IN ('INCOME', 'DEBT_RECEIVED', 'DEBT_REPAID_TO_US') THEN amountKopecks
				ELSE -amountKopecks
			END
		), 0)
		FROM cash_transactions
		"""
	)
	fun observeBalance(): Flow<Long>

	@Query(
		"""
		SELECT COALESCE(SUM(
			CASE
				WHEN type IN ('INCOME', 'DEBT_RECEIVED', 'DEBT_REPAID_TO_US') THEN amountKopecks
				ELSE -amountKopecks
			END
		), 0)
		FROM cash_transactions
		"""
	)
	suspend fun getBalance(): Long

	@Query(
		"""
		SELECT COALESCE(SUM(amountKopecks), 0) FROM cash_transactions
		WHERE documentId IS NULL AND type = 'INCOME'
		"""
	)
	fun observeManualIncome(): Flow<Long>

	@Query(
		"""
		SELECT COALESCE(SUM(amountKopecks), 0) FROM cash_transactions
		WHERE documentId IS NULL AND type = 'EXPENSE'
		"""
	)
	fun observeManualExpense(): Flow<Long>

	@Query(
		"""
		SELECT COALESCE(SUM(
			CASE
				WHEN type IN ('INCOME', 'DEBT_RECEIVED', 'DEBT_REPAID_TO_US') THEN amountKopecks
				ELSE -amountKopecks
			END
		), 0)
		FROM cash_transactions
		"""
	)
	fun observeCashFlow(): Flow<Long>
}
