package ru.itis.kpfu.ermstore.ui.cash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.itis.kpfu.ermstore.domain.calc.CashMath
import ru.itis.kpfu.ermstore.domain.model.CashFilter
import ru.itis.kpfu.ermstore.domain.model.CashOperationType
import ru.itis.kpfu.ermstore.domain.model.CashTransaction
import ru.itis.kpfu.ermstore.ui.components.AddButton
import ru.itis.kpfu.ermstore.ui.components.EmptyState
import ru.itis.kpfu.ermstore.ui.components.LoadingState
import ru.itis.kpfu.ermstore.ui.components.ScreenScaffold
import ru.itis.kpfu.ermstore.ui.components.SelectionField
import ru.itis.kpfu.ermstore.ui.util.cashFilterLabel
import ru.itis.kpfu.ermstore.ui.util.cashTypeLabel
import ru.itis.kpfu.ermstore.ui.util.formatDate
import ru.itis.kpfu.ermstore.ui.util.formatMoney
import ru.itis.kpfu.ermstore.ui.util.parseMoneyToKopecks

@Composable
fun CashScreen(viewModel: CashViewModel = hiltViewModel()) {
	val state by viewModel.state.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	var showAddDialog by remember { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		viewModel.events.collect { snackbarHostState.showSnackbar(it) }
	}

	ScreenScaffold(
		title = "Касса",
		floatingActionButton = { AddButton(onClick = { showAddDialog = true }) },
		snackbarHost = { SnackbarHost(snackbarHostState) }
	) { padding ->
		when {
			state.isLoading -> LoadingState(Modifier.padding(padding))
			else -> LazyColumn(
				modifier = Modifier.fillMaxSize().padding(padding),
				contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				item {
					BalanceCard(state.balanceKopecks)
				}
				item {
					LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
						items(CashFilter.entries) { filter ->
							FilterChip(
								selected = state.filter == filter,
								onClick = { viewModel.setFilter(filter) },
								label = { Text(cashFilterLabel(filter)) }
							)
						}
					}
				}
				item {
					Text("Операции", style = MaterialTheme.typography.titleMedium)
				}
				if (state.transactions.isEmpty()) {
					item {
						EmptyState(
							text = if (state.filter == CashFilter.ALL) {
								"Операций пока нет"
							} else {
								"Операций по фильтру нет"
							}
						)
					}
				} else {
					items(state.transactions, key = { it.id }) { transaction ->
						CashTransactionCard(transaction)
					}
				}
			}
		}
	}

	if (showAddDialog) {
		AddCashDialog(
			onDismiss = { showAddDialog = false },
			onConfirm = { type, amountKopecks, comment ->
				viewModel.add(type, amountKopecks, comment)
				showAddDialog = false
			}
		)
	}
}

@Composable
private fun BalanceCard(balanceKopecks: Long) {
	Card(modifier = Modifier.fillMaxWidth()) {
		Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
			Text(
				text = "Остаток в кассе",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Text(
				text = formatMoney(balanceKopecks),
				style = MaterialTheme.typography.headlineMedium
			)
		}
	}
}

@Composable
private fun CashTransactionCard(transaction: CashTransaction) {
	val signedAmount = CashMath.signedAmount(transaction.type, transaction.amountKopecks)
	Card(modifier = Modifier.fillMaxWidth()) {
		Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
			Column {
				Text(cashTypeLabel(transaction.type), style = MaterialTheme.typography.titleMedium)
				Text(
					text = formatDate(transaction.dateMillis),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			Text(
				text = formatMoney(signedAmount),
				style = MaterialTheme.typography.titleMedium,
				color = if (signedAmount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
			)
			if (!transaction.comment.isNullOrBlank()) {
				Text(transaction.comment, style = MaterialTheme.typography.bodySmall)
			}
			if (transaction.documentId != null) {
				Text(
					text = "Связано с документом",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

@Composable
private fun AddCashDialog(
	onDismiss: () -> Unit,
	onConfirm: (CashOperationType, Long, String?) -> Unit
) {
	var type by remember { mutableStateOf(CashOperationType.INCOME) }
	var amount by remember { mutableStateOf("") }
	var comment by remember { mutableStateOf("") }
	var error by remember { mutableStateOf<String?>(null) }
	val typeOptions = CashOperationType.entries.map { it to cashTypeLabel(it) }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("Новая операция") },
		text = {
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				SelectionField(
					label = "Тип операции",
					selectedText = cashTypeLabel(type),
					options = typeOptions,
					onSelected = { type = it }
				)
				OutlinedTextField(
					value = amount,
					onValueChange = { amount = it.filter { char -> char.isDigit() || char == ',' || char == '.' } },
					label = { Text("Сумма, ₽") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
				)
				OutlinedTextField(
					value = comment,
					onValueChange = { comment = it },
					label = { Text("Комментарий") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true
				)
				if (error != null) {
					Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
				}
			}
		},
		confirmButton = {
			TextButton(
				onClick = {
					val kopecks = parseMoneyToKopecks(amount)
					if (kopecks == null || kopecks <= 0) {
						error = "Укажите положительную сумму"
					} else {
						onConfirm(type, kopecks, comment.ifBlank { null })
					}
				}
			) {
				Text("Добавить")
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text("Отмена")
			}
		}
	)
}
