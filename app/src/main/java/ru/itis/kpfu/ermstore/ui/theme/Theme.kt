package ru.itis.kpfu.ermstore.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
	primary = Color(0xFF006C4C),
	onPrimary = Color.White,
	primaryContainer = Color(0xFF8EF8C6),
	onPrimaryContainer = Color(0xFF002116),
	secondary = Color(0xFF4D6357),
	onSecondary = Color.White,
	secondaryContainer = Color(0xFFCFE9D9),
	onSecondaryContainer = Color(0xFF0A1F16),
	tertiary = Color(0xFF3D6473),
	onTertiary = Color.White,
	tertiaryContainer = Color(0xFFC1E8FA),
	onTertiaryContainer = Color(0xFF001F29),
	background = Color(0xFFF5FBF5),
	onBackground = Color(0xFF171D19),
	surface = Color(0xFFF5FBF5),
	onSurface = Color(0xFF171D19),
	surfaceVariant = Color(0xFFDBE5DD),
	onSurfaceVariant = Color(0xFF404944),
	error = Color(0xFFBA1A1A),
	onError = Color.White
)

private val DarkColors = darkColorScheme(
	primary = Color(0xFF72DBA8),
	onPrimary = Color(0xFF003826),
	primaryContainer = Color(0xFF005138),
	onPrimaryContainer = Color(0xFF8EF8C6),
	secondary = Color(0xFFB3CDBD),
	onSecondary = Color(0xFF1F352A),
	secondaryContainer = Color(0xFF354B40),
	onSecondaryContainer = Color(0xFFCFE9D9),
	tertiary = Color(0xFFA5CCDE),
	onTertiary = Color(0xFF073543),
	tertiaryContainer = Color(0xFF244C5B),
	onTertiaryContainer = Color(0xFFC1E8FA),
	background = Color(0xFF0F1511),
	onBackground = Color(0xFFDEE4DE),
	surface = Color(0xFF0F1511),
	onSurface = Color(0xFFDEE4DE),
	surfaceVariant = Color(0xFF404944),
	onSurfaceVariant = Color(0xFFBFC9C1),
	error = Color(0xFFFFB4AB),
	onError = Color(0xFF690005)
)

@Composable
fun ErmStoreTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	MaterialTheme(
		colorScheme = if (darkTheme) DarkColors else LightColors,
		content = content
	)
}
