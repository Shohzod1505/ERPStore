package ru.itis.kpfu.ermstore.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.itis.kpfu.ermstore.domain.model.CashOperationType
import ru.itis.kpfu.ermstore.domain.model.CashTransaction
import ru.itis.kpfu.ermstore.domain.repository.CashRepository
import javax.inject.Inject

class ObserveCashTransactionsUseCase @Inject constructor(
	private val repository: CashRepository
) {
	operator fun invoke(): Flow<List<CashTransaction>> = repository.observeTransactions()
}

class ObserveCashBalanceUseCase @Inject constructor(
	private val repository: CashRepository
) {
	operator fun invoke(): Flow<Long> = repository.observeBalance()
}

class AddCashOperationUseCase @Inject constructor(
	private val repository: CashRepository
) {
	suspend operator fun invoke(
		type: CashOperationType,
		amountKopecks: Long,
		comment: String?
	) = repository.addManual(type, amountKopecks, comment)
}
