package com.pecule.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = MintPrimary,
    onPrimary = MintOnPrimary,
    background = MintBackground,
    surface = MintSurface,
    onBackground = MintTextPrimary,
    onSurface = MintTextPrimary,
    secondary = MintTextSecondary,
    onSecondary = MintOnPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = MintPrimaryDark,
    onPrimary = MintOnPrimaryDark,
    background = MintBackgroundDark,
    surface = MintSurfaceDark,
    onBackground = MintTextPrimaryDark,
    onSurface = MintTextPrimaryDark,
    secondary = MintTextSecondaryDark,
    onSecondary = MintOnPrimaryDark
)

@Composable
fun PeculeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = PeculeShapes,
        typography = Typography,
        content = content
    )
}
