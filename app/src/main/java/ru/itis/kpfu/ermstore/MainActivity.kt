package ru.itis.kpfu.ermstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import ru.itis.kpfu.ermstore.ui.navigation.ErmStoreNavHost
import ru.itis.kpfu.ermstore.ui.theme.ErmStoreTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			ErmStoreTheme {
				ErmStoreNavHost()
			}
		}
	}
}
