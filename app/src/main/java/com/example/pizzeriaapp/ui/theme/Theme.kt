package com.example.pizzeriaapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PizzeriaColorScheme = lightColorScheme(
    primary = PizzeriaPrimary,
    onPrimary = PizzeriaOnPrimary,
    primaryContainer = PizzeriaPrimaryContainer,
    onPrimaryContainer = PizzeriaOnPrimaryContainer,
    secondary = PizzeriaSecondary,
    onSecondary = PizzeriaOnSecondary,
    secondaryContainer = PizzeriaSecondaryContainer,
    onSecondaryContainer = PizzeriaOnSecondaryContainer,
    tertiary = PizzeriaTertiary,
    onTertiary = PizzeriaOnTertiary,
    tertiaryContainer = PizzeriaTertiaryContainer,
    onTertiaryContainer = PizzeriaOnTertiaryContainer,
    background = PizzeriaBackground,
    onBackground = PizzeriaOnBackground,
    surface = PizzeriaSurface,
    onSurface = PizzeriaOnSurface,
    surfaceVariant = PizzeriaSurfaceVariant,
    onSurfaceVariant = PizzeriaOnSurfaceVariant,
    outline = PizzeriaOutline,
    error = PizzeriaError,
    onError = PizzeriaOnError,
    errorContainer = PizzeriaErrorContainer,
    onErrorContainer = PizzeriaOnErrorContainer
)

@Composable
fun PizzeriaAppTheme(
    content: @Composable () -> Unit
) {
    // Color y formas fijos: misma apariencia en registro, pedido, login y trabajador.
    MaterialTheme(
        colorScheme = PizzeriaColorScheme,
        typography = Typography,
        shapes = PizzeriaShapes,
        content = content
    )
}
