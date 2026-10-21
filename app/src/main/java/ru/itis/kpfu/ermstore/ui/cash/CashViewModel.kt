package ru.itis.kpfu.ermstore.ui.cash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.itis.kpfu.ermstore.domain.model.CashOperationType
import ru.itis.kpfu.ermstore.domain.model.CashTransaction
import ru.itis.kpfu.ermstore.domain.usecase.AddCashOperationUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveCashBalanceUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveCashTransactionsUseCase
import javax.inject.Inject

data class CashUiState(
	val balanceKopecks: Long = 0,
	val transactions: List<CashTransaction> = emptyList(),
	val isLoading: Boolean = true
)

@HiltViewModel
class CashViewModel @Inject constructor(
	observeTransactions: ObserveCashTransactionsUseCase,
	observeBalance: ObserveCashBalanceUseCase,
	private val addCashOperation: AddCashOperationUseCase
) : ViewModel() {
	val state: StateFlow<CashUiState> = combine(
		observeTransactions(),
		observeBalance()
	) { transactions, balance ->
		CashUiState(
			balanceKopecks = balance,
			transactions = transactions,
			isLoading = false
		)
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = CashUiState()
	)

	private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
	val events = _events.asSharedFlow()

	fun add(type: CashOperationType, amountKopecks: Long, comment: String?) {
		viewModelScope.launch {
			runCatching {
				addCashOperation(type, amountKopecks, comment)
			}.onSuccess {
				_events.emit("Операция добавлена")
			}.onFailure {
				_events.emit("Не удалось добавить операцию")
			}
		}
	}
}
