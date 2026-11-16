package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFBCAAA4),
    onPrimary = Color(0xFF3E2723),
    primaryContainer = Color(0xFF5D4037),
    onPrimaryContainer = Color(0xFFD7CCC8),
    secondary = Color(0xFFD7CCC8),
    onSecondary = Color(0xFF3E2723),
    background = Color(0xFF1E1B18),
    surface = Color(0xFF2A2521),
    onSurface = Color(0xFFEFEBE9)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ChocolatePrimary,
    onPrimary = ChocolateOnPrimary,
    primaryContainer = ChocolateContainer,
    onPrimaryContainer = ChocolateOnContainer,
    secondary = Color(0xFF6D4C41),
    onSecondary = Color.White,
    background = TruffleBeige,
    surface = TruffleSurface,
    onSurface = TruffleOnSurface
  )

@Composable
fun TrufasBellaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
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

// Alias for compatibility if referenced
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  TrufasBellaTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
