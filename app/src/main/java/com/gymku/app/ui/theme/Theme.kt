package com.gymku.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val GymKuColorScheme = lightColorScheme(
    primary          = IndigoMain,
    onPrimary        = White,
    primaryContainer = IndigoLight,
    onPrimaryContainer = IndigoDark,
    secondary        = EmeraldGreen,
    onSecondary      = White,
    secondaryContainer = EmeraldLight,
    onSecondaryContainer = EmeraldGreen,
    error            = RoseRed,
    onError          = White,
    errorContainer   = RoseLight,
    onErrorContainer = RoseRed,
    background       = Slate50,
    onBackground     = Slate900,
    surface          = White,
    onSurface        = Slate900,
    surfaceVariant   = Slate100,
    onSurfaceVariant = Slate500,
    outline          = Slate200,
    outlineVariant   = Slate100
)

@Composable
fun GymKuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GymKuColorScheme,
        typography  = GymKuTypography,
        content     = content
    )
}
