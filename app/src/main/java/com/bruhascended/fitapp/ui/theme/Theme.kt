package com.bruhascended.fitapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors(
	primary = Blue500,
	primaryVariant = White700,
	secondary = Blue500,
	secondaryVariant = Blue700,

    background = White700,
    surface = White700,

    onPrimary = Black700,
    onSecondary = Black700,
    onBackground = Black700,
    onSurface = Black700,
)

private val DarkColorPalette = darkColors(
	primary = Blue500,
	primaryVariant = Black700,
	secondary = Blue500,
	secondaryVariant = Blue200,

	background = Black700,
	surface = Black700,

	onPrimary = White700,
	onSecondary = White700,
	onBackground = White700,
	onSurface = White700,
)


@Composable
fun FitAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
	val colors = if (darkTheme) {
		DarkColorPalette
	} else {
		LightColorPalette
	}

	MaterialTheme(
		colors = colors,
		typography = Typography,
		shapes = Shapes,
		content = content
	)
}