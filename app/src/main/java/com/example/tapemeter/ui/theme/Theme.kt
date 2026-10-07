package com.example.tapemeter.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.tapemeter.data.model.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = KraftAmberLight,
    onPrimary = SlateDark,
    primaryContainer = KraftAmberDark,
    onPrimaryContainer = KraftAmberLight,
    secondary = IndustrialTealDark,
    onSecondary = SlateDark,
    secondaryContainer = SlateSurfaceVariantDark,
    onSecondaryContainer = SlateTextPrimaryDark,
    tertiary = TapePathDark,
    onTertiary = SlateDark,
    background = SlateDark,
    onBackground = SlateTextPrimaryDark,
    surface = SlateSurfaceDark,
    onSurface = SlateTextPrimaryDark,
    surfaceVariant = SlateSurfaceVariantDark,
    onSurfaceVariant = SlateTextSecondaryDark,
    outline = SlateBorderDark,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = KraftAmber,
    onPrimary = SlateSurfaceLight,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = KraftAmberDark,
    secondary = IndustrialTeal,
    onSecondary = SlateSurfaceLight,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = TapePathLight,
    onTertiary = SlateSurfaceLight,
    background = SlateLight,
    onBackground = SlateTextPrimaryLight,
    surface = SlateSurfaceLight,
    onSurface = SlateTextPrimaryLight,
    surfaceVariant = SlateSurfaceVariantLight,
    onSurfaceVariant = SlateTextSecondaryLight,
    outline = SlateBorderLight,
    error = ErrorRed
)

@Composable
fun TapeMeterTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}