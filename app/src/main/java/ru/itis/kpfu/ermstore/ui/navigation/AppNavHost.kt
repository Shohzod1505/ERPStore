package ru.itis.kpfu.ermstore.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.itis.kpfu.ermstore.ui.cash.CashScreen
import ru.itis.kpfu.ermstore.ui.documents.DocumentDetailScreen
import ru.itis.kpfu.ermstore.ui.documents.DocumentFormScreen
import ru.itis.kpfu.ermstore.ui.documents.DocumentsScreen
import ru.itis.kpfu.ermstore.ui.locations.LocationsScreen
import ru.itis.kpfu.ermstore.ui.products.ProductsScreen
import ru.itis.kpfu.ermstore.ui.reports.ReportsScreen

private data class BottomDestination(
	val route: String,
	val label: String,
	val short: String
)

private val bottomDestinations = listOf(
	BottomDestination("products", "Товары", "Т"),
	BottomDestination("locations", "Склады", "С"),
	BottomDestination("documents", "Документы", "Д"),
	BottomDestination("cash", "Касса", "К"),
	BottomDestination("reports", "Отчёты", "О")
)

@Composable
fun ErmStoreNavHost() {
	val navController = rememberNavController()
	val backStackEntry by navController.currentBackStackEntryAsState()
	val currentRoute = backStackEntry?.destination?.route
	val documentsSelected = currentRoute == "documents" || currentRoute?.startsWith("document/") == true

	Scaffold(
		contentWindowInsets = WindowInsets(0, 0, 0, 0),
		bottomBar = {
			NavigationBar {
				bottomDestinations.forEach { destination ->
					val selected = when (destination.route) {
						"documents" -> documentsSelected
						else -> currentRoute == destination.route
					}
					NavigationBarItem(
						selected = selected,
						onClick = {
							navController.navigate(destination.route) {
								popUpTo(navController.graph.findStartDestination().id) {
									saveState = true
								}
								launchSingleTop = true
								restoreState = true
							}
						},
						icon = { Text(destination.short, style = MaterialTheme.typography.titleMedium) },
						label = { Text(destination.label) }
					)
				}
			}
		}
	) { innerPadding ->
		NavHost(
			navController = navController,
			startDestination = "products",
			modifier = Modifier.padding(innerPadding)
		) {
			composable("products") {
				ProductsScreen()
			}
			composable("locations") {
				LocationsScreen()
			}
			composable("documents") {
				DocumentsScreen(
					onAdd = { type ->
						navController.navigate("document/new/${type.name}")
					},
					onOpen = { documentId ->
						navController.navigate("document/$documentId")
					}
				)
			}
			composable("cash") {
				CashScreen()
			}
			composable("reports") {
				ReportsScreen()
			}
			composable(
				route = "document/new/{type}",
				arguments = listOf(navArgument("type") { type = NavType.StringType })
			) { entry ->
				DocumentFormScreen(
					onBack = { navController.popBackStack() },
					onSaved = { documentId ->
						navController.navigate("document/$documentId") {
							popUpTo(entry.destination.id) { inclusive = true }
						}
					}
				)
			}
			composable(
				route = "document/{documentId}",
				arguments = listOf(navArgument("documentId") { type = NavType.LongType })
			) {
				DocumentDetailScreen(
					onBack = { navController.popBackStack() }
				)
			}
		}
	}
}
