package ru.itis.kpfu.ermstore.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
	title: String,
	onBack: (() -> Unit)? = null,
	actions: @Composable RowScope.() -> Unit = {},
	floatingActionButton: @Composable () -> Unit = {},
	snackbarHost: @Composable () -> Unit = {},
	content: @Composable (PaddingValues) -> Unit
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(
						text = title,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				},
				navigationIcon = {
					if (onBack != null) {
						TextButton(onClick = onBack) {
							Text("←", style = MaterialTheme.typography.titleLarge)
						}
					}
				},
				actions = actions
			)
		},
		floatingActionButton = floatingActionButton,
		snackbarHost = snackbarHost,
		content = content
	)
}

@Composable
fun AddButton(onClick: () -> Unit) {
	FloatingActionButton(onClick = onClick) {
		Text("+", style = MaterialTheme.typography.headlineSmall)
	}
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
	Box(
		modifier = modifier.fillMaxSize().padding(24.dp),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = text,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			textAlign = TextAlign.Center
		)
	}
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
	Box(
		modifier = modifier.fillMaxSize().padding(24.dp),
		contentAlignment = Alignment.Center
	) {
		CircularProgressIndicator()
	}
}

@Composable
fun <T> SelectionField(
	label: String,
	selectedText: String?,
	options: List<Pair<T, String>>,
	onSelected: (T) -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true
) {
	var expanded by remember { mutableStateOf(false) }
	Column(modifier = modifier) {
		OutlinedButton(
			onClick = { expanded = true },
			modifier = Modifier.fillMaxWidth(),
			enabled = enabled
		) {
			Text(
				text = selectedText ?: label,
				modifier = Modifier.weight(1f),
				textAlign = TextAlign.Start
			)
			Text("▼")
		}
		DropdownMenu(
			expanded = expanded,
			onDismissRequest = { expanded = false }
		) {
			options.forEach { (value, text) ->
				DropdownMenuItem(
					text = { Text(text) },
					onClick = {
						expanded = false
						onSelected(value)
					}
				)
			}
		}
	}
}

@Composable
fun ConfirmDialog(
	title: String,
	text: String,
	confirmText: String,
	onConfirm: () -> Unit,
	onDismiss: () -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(title) },
		text = { Text(text) },
		confirmButton = {
			TextButton(onClick = onConfirm) {
				Text(confirmText)
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text("Отмена")
			}
		}
	)
}

@Composable
fun KeyValueRow(
	label: String,
	value: String,
	modifier: Modifier = Modifier,
	emphasized: Boolean = false
) {
	Row(
		modifier = modifier.fillMaxWidth().padding(vertical = 3.dp),
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
		Text(
			text = value,
			style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
		)
	}
}
