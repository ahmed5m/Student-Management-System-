package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkTealPrimary,
    secondary = DarkMintSecondary,
    tertiary = DarkTealPrimary,
    background = DeepDarkBackground,
    surface = DeepDarkSurface,
    onPrimary = DeepDarkAccent,
    onSecondary = DeepDarkAccent,
    onBackground = SoftBackground,
    onSurface = SoftBackground
)

private val LightColorScheme = lightColorScheme(
    primary = DeepTealPrimary,
    secondary = MintSecondary,
    tertiary = ForestAccent,
    background = SoftBackground,
    surface = CleanSurface,
    onPrimary = CleanSurface,
    onSecondary = CleanSurface,
    onBackground = DeepDarkAccent,
    onSurface = DeepDarkAccent
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disable dynamicColor here to enforce our custom elegant Educational system branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
