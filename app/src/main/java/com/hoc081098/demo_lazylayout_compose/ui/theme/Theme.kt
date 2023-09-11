package com.hoc081098.demo_lazylayout_compose.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
  primary = Purple80,
  onPrimary = Purple20,
  primaryContainer = Purple30,
  onPrimaryContainer = Purple90,
  secondary = Orange80,
  onSecondary = Orange20,
  secondaryContainer = Orange30,
  onSecondaryContainer = Orange90,
  tertiary = Blue80,
  onTertiary = Blue20,
  tertiaryContainer = Blue30,
  onTertiaryContainer = Blue90,
  error = Red80,
  onError = Red20,
  errorContainer = Red30,
  onErrorContainer = Red90,
  background = DarkPurpleGray10,
  onBackground = DarkPurpleGray90,
  surface = DarkPurpleGray10,
  onSurface = DarkPurpleGray90,
  surfaceVariant = PurpleGray30,
  onSurfaceVariant = PurpleGray80,
  inverseSurface = DarkPurpleGray90,
  inverseOnSurface = DarkPurpleGray10,
  outline = PurpleGray60,
)

private val LightColorScheme = lightColorScheme(
  primary = Purple40,
  onPrimary = Color.White,
  primaryContainer = Purple90,
  onPrimaryContainer = Purple10,
  secondary = Orange40,
  onSecondary = Color.White,
  secondaryContainer = Orange90,
  onSecondaryContainer = Orange10,
  tertiary = Blue40,
  onTertiary = Color.White,
  tertiaryContainer = Blue90,
  onTertiaryContainer = Blue10,
  error = Red40,
  onError = Color.White,
  errorContainer = Red90,
  onErrorContainer = Red10,
  background = DarkPurpleGray99,
  onBackground = DarkPurpleGray10,
  surface = DarkPurpleGray99,
  onSurface = DarkPurpleGray10,
  surfaceVariant = PurpleGray90,
  onSurfaceVariant = PurpleGray30,
  inverseSurface = DarkPurpleGray20,
  inverseOnSurface = DarkPurpleGray95,
  outline = PurpleGray50,
)

@Composable
fun DemoLazyLayoutComposeTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }

    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.primary.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}