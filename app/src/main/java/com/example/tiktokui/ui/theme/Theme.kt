package com.example.tiktokui.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TikTokAccent,
    secondary = TikTokDarkTextSecondary,
    tertiary = TikTokLike,
    background = TikTokDarkSurface,
    surface = TikTokDarkSurface,
    surfaceVariant = TikTokDarkSurfaceVariant,
    onPrimary = TikTokSurface,
    onSecondary = TikTokDarkSurface,
    onBackground = TikTokDarkTextPrimary,
    onSurface = TikTokDarkTextPrimary,
    onSurfaceVariant = TikTokDarkTextSecondary,
    outline = TikTokOutline
)

private val LightColorScheme = lightColorScheme(
    primary = TikTokAccent,
    secondary = TikTokTextSecondary,
    tertiary = TikTokLike,
    background = TikTokBackground,
    surface = TikTokSurface,
    surfaceVariant = TikTokSurfaceVariant,
    onPrimary = TikTokSurface,
    onSecondary = TikTokSurface,
    onBackground = TikTokTextPrimary,
    onSurface = TikTokTextPrimary,
    onSurfaceVariant = TikTokTextSecondary,
    outline = TikTokOutline
)

@Composable
fun TikTokUITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
