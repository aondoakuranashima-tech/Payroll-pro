package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.EnterpriseColorTheme
import com.example.data.model.ThemeMode

fun buildEnterpriseColorScheme(
  colorTheme: EnterpriseColorTheme,
  isDark: Boolean,
  highContrast: Boolean = false
): ColorScheme {
  val primaryColor = Color(colorTheme.primaryHex)
  val primaryContainer = Color(colorTheme.primaryContainerHex)
  val onPrimaryContainer = Color(colorTheme.onPrimaryContainerHex)
  val secondaryColor = Color(colorTheme.secondaryHex)

  return if (isDark) {
    darkColorScheme(
      primary = if (highContrast) Color.White else primaryColor.copy(alpha = 0.92f),
      onPrimary = Color(colorTheme.backgroundDarkHex),
      primaryContainer = Color(colorTheme.surfaceDarkHex),
      onPrimaryContainer = Color(colorTheme.primaryContainerHex),
      secondary = secondaryColor,
      onSecondary = Color.White,
      secondaryContainer = Color(colorTheme.surfaceDarkHex),
      onSecondaryContainer = Color(colorTheme.primaryContainerHex),
      tertiary = VibrantTertiary,
      onTertiary = Color.White,
      background = Color(colorTheme.backgroundDarkHex),
      surface = Color(colorTheme.surfaceDarkHex),
      surfaceVariant = Color(colorTheme.surfaceDarkHex).copy(alpha = 0.7f),
      outline = if (highContrast) Color.White.copy(alpha = 0.6f) else Color(colorTheme.secondaryHex).copy(alpha = 0.35f),
      outlineVariant = Color(colorTheme.secondaryHex).copy(alpha = 0.2f),
      error = VibrantError,
      errorContainer = VibrantErrorContainer
    )
  } else {
    lightColorScheme(
      primary = primaryColor,
      onPrimary = Color.White,
      primaryContainer = primaryContainer,
      onPrimaryContainer = onPrimaryContainer,
      secondary = secondaryColor,
      onSecondary = Color.White,
      secondaryContainer = primaryContainer.copy(alpha = 0.65f),
      onSecondaryContainer = onPrimaryContainer,
      tertiary = VibrantTertiary,
      onTertiary = Color.White,
      tertiaryContainer = VibrantTertiaryContainer,
      onTertiaryContainer = VibrantOnTertiaryContainer,
      background = if (highContrast) Color.White else Color(0xFFF8F9FE),
      surface = Color.White,
      surfaceVariant = Color(0xFFE8EDF5),
      outline = if (highContrast) Color.Black else Color(0xFFC4D0E3),
      outlineVariant = Color(0xFFE2E8F0),
      error = VibrantError,
      errorContainer = VibrantErrorContainer
    )
  }
}

private val DarkColorScheme = buildEnterpriseColorScheme(EnterpriseColorTheme.SAPPHIRE_BLUE, isDark = true)
private val LightColorScheme = buildEnterpriseColorScheme(EnterpriseColorTheme.SAPPHIRE_BLUE, isDark = false)

@Composable
fun MyApplicationTheme(
  themeMode: ThemeMode = ThemeMode.SYSTEM,
  colorTheme: EnterpriseColorTheme = EnterpriseColorTheme.SAPPHIRE_BLUE,
  dynamicColor: Boolean = false,
  highContrast: Boolean = false,
  content: @Composable () -> Unit,
) {
  val systemDark = isSystemInDarkTheme()
  val isDark = when (themeMode) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.CUSTOM -> systemDark
  }

  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    themeMode == ThemeMode.CUSTOM || colorTheme != EnterpriseColorTheme.SAPPHIRE_BLUE -> {
      buildEnterpriseColorScheme(colorTheme, isDark, highContrast)
    }
    isDark -> buildEnterpriseColorScheme(EnterpriseColorTheme.SAPPHIRE_BLUE, isDark = true, highContrast)
    else -> buildEnterpriseColorScheme(EnterpriseColorTheme.SAPPHIRE_BLUE, isDark = false, highContrast)
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

