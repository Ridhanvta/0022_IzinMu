package com.example.izinmu.ui.theme

import com.example.izinmu.ui.theme.Dimens
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val IzinColorScheme = lightColorScheme(
    primary = IzinNavy,
    onPrimary = IzinSurface,
    primaryContainer = IzinBlueContainer,
    onPrimaryContainer = IzinNavyDark,
    secondary = IzinNavySoft,
    onSecondary = IzinSurface,
    secondaryContainer = IzinBlueContainer,
    onSecondaryContainer = IzinNavyDark,
    tertiary = IzinInstructor,
    onTertiary = IzinSurface,
    background = IzinBackground,
    onBackground = IzinText,
    surface = IzinSurface,
    onSurface = IzinText,
    surfaceVariant = IzinBackground,
    onSurfaceVariant = IzinMuted,
    outline = IzinOutline,
    outlineVariant = IzinOutline,
    error = IzinDanger,
    onError = IzinSurface,
    errorContainer = IzinDangerContainer,
    onErrorContainer = IzinDanger,
)

private val IzinShapes = Shapes(
    extraSmall = RoundedCornerShape(Dimens.s8),
    small = RoundedCornerShape(Dimens.s10),
    medium = RoundedCornerShape(Dimens.s14),
    large = RoundedCornerShape(Dimens.s20),
    extraLarge = RoundedCornerShape(Dimens.s28),
)

@Composable
fun IzinMuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IzinColorScheme,
        typography = Typography,
        shapes = IzinShapes,
        content = content,
    )
}
