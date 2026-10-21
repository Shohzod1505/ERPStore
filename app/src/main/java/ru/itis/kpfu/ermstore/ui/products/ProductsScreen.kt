package ru.itis.kpfu.ermstore.ui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.itis.kpfu.ermstore.ui.components.AddButton
import ru.itis.kpfu.ermstore.ui.components.EmptyState
import ru.itis.kpfu.ermstore.ui.components.LoadingState
import ru.itis.kpfu.ermstore.ui.components.ScreenScaffold
import ru.itis.kpfu.ermstore.ui.util.formatQuantity

@Composable
fun ProductsScreen(
	onOpen: (Long) -> Unit,
	viewModel: ProductsViewModel = hiltViewModel()
) {
	val state by viewModel.state.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	var showAddDialog by remember { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		viewModel.events.collect { snackbarHostState.showSnackbar(it) }
	}

	ScreenScaffold(
		title = "Товары",
		floatingActionButton = { AddButton(onClick = { showAddDialog = true }) },
		snackbarHost = { SnackbarHost(snackbarHostState) }
	) { padding ->
		when {
			state.isLoading -> LoadingState(Modifier.padding(padding))
			state.rows.isEmpty() -> EmptyState(
				text = "Товаров пока нет. Добавьте первый товар.",
				modifier = Modifier.padding(padding)
			)
			else -> LazyColumn(
				modifier = Modifier.fillMaxSize().padding(padding),
				contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				items(state.rows, key = { it.id }) { product ->
					ProductCard(product, onClick = { onOpen(product.id) })
				}
			}
		}
	}

	if (showAddDialog) {
		AddProductDialog(
			onDismiss = { showAddDialog = false },
			onConfirm = { name, unitsPerBlock ->
				viewModel.create(name, unitsPerBlock)
				showAddDialog = false
			}
		)
	}
}

@Composable
private fun ProductCard(product: ProductRowUi, onClick: () -> Unit) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
	) {
		Column(modifier = Modifier.padding(16.dp)) {
			Text(
				text = product.name,
				style = MaterialTheme.typography.titleMedium,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				text = formatQuantity(product.units, product.unitsPerBlock),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Text(
				text = "В блоке: ${product.unitsPerBlock} шт.",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

@Composable
private fun AddProductDialog(
	onDismiss: () -> Unit,
	onConfirm: (String, Int) -> Unit
) {
	var name by remember { mutableStateOf("") }
	var unitsPerBlock by remember { mutableStateOf("") }
	var error by remember { mutableStateOf<String?>(null) }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("Новый товар") },
		text = {
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				OutlinedTextField(
					value = name,
					onValueChange = { name = it },
					label = { Text("Название") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true
				)
				OutlinedTextField(
					value = unitsPerBlock,
					onValueChange = { unitsPerBlock = it.filter(Char::isDigit) },
					label = { Text("Штук в блоке") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
				)
				if (error != null) {
					Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
				}
			}
		},
		confirmButton = {
			TextButton(
				onClick = {
					val count = unitsPerBlock.toIntOrNull()
					if (name.isBlank() || count == null || count <= 0) {
						error = "Укажите название и положительное число штук в блоке"
					} else {
						onConfirm(name, count)
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
