package ru.itis.kpfu.ermstore.ui.cash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.itis.kpfu.ermstore.domain.model.CashFilter
import ru.itis.kpfu.ermstore.domain.model.CashOperationError
import ru.itis.kpfu.ermstore.domain.model.CashOperationResult
import ru.itis.kpfu.ermstore.domain.model.CashOperationType
import ru.itis.kpfu.ermstore.domain.model.CashTransaction
import ru.itis.kpfu.ermstore.domain.usecase.AddCashOperationUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveCashBalanceUseCase
import ru.itis.kpfu.ermstore.domain.usecase.ObserveCashTransactionsUseCase
import ru.itis.kpfu.ermstore.ui.util.formatMoney
import javax.inject.Inject

data class CashUiState(
	val balanceKopecks: Long = 0,
	val transactions: List<CashTransaction> = emptyList(),
	val filter: CashFilter = CashFilter.ALL,
	val isLoading: Boolean = true
)

@HiltViewModel
class CashViewModel @Inject constructor(
	observeTransactions: ObserveCashTransactionsUseCase,
	observeBalance: ObserveCashBalanceUseCase,
	private val addCashOperation: AddCashOperationUseCase
) : ViewModel() {
	private val filter = MutableStateFlow(CashFilter.ALL)
	val state: StateFlow<CashUiState> = combine(
		observeTransactions(),
		observeBalance(),
		filter
	) { transactions, balance, selectedFilter ->
		CashUiState(
			balanceKopecks = balance,
			transactions = transactions.filter { selectedFilter.matches(it.type) },
			filter = selectedFilter,
			isLoading = false
		)
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = CashUiState()
	)

	private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
	val events = _events.asSharedFlow()

	fun setFilter(value: CashFilter) {
		filter.value = value
	}

	fun add(type: CashOperationType, amountKopecks: Long, comment: String?) {
		viewModelScope.launch {
			runCatching {
				addCashOperation(type, amountKopecks, comment)
			}.onSuccess { result ->
				_events.emit(
					when (result) {
						CashOperationResult.Success -> "Операция добавлена"
						is CashOperationResult.Failure -> cashErrorText(result.error)
					}
				)
			}.onFailure {
				_events.emit("Не удалось добавить операцию")
			}
		}
	}

	private fun cashErrorText(error: CashOperationError): String = when (error) {
		CashOperationError.InvalidAmount -> "Сумма должна быть положительной"
		is CashOperationError.InsufficientFunds ->
			"Недостаточно денег в кассе: доступно ${formatMoney(error.availableKopecks)}, нужно ${formatMoney(error.requestedKopecks)}"
	}
}
