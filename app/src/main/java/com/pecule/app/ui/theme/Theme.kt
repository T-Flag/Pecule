package com.pecule.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.pecule.app.data.local.datastore.ThemePreference

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
    themePreference: ThemePreference = ThemePreference.AUTO,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themePreference) {
        ThemePreference.AUTO -> darkTheme
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = PeculeShapes,
        typography = Typography,
        content = content
    )
}
