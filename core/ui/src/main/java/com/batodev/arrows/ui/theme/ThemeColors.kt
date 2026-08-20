package com.batodev.arrows.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class ThemeColors(
    val background: Color,
    val accent: Color,
    val snake: Color,
    val topBarButton: Color,
    val bottomBar: Color,
)

val LocalThemeColors =
    staticCompositionLocalOf {
        ThemeColors(
            background = DarkBackground,
            accent = AccentBlue,
            snake = SnakeColor,
            topBarButton = TopBarButtonBackground,
            bottomBar = BottomBarBackground,
        )
    }

private val DarkColorScheme = darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)
private val LightColorScheme = lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

private fun getThemeColors(themeName: String): ThemeColors =
    when (themeName) {
        "Green" -> {
            ThemeColors(
                GreenBackground,
                GreenAccent,
                GreenSnake,
                GreenAccent.copy(alpha = 0.2f),
                GreenBackground.copy(alpha = 0.8f),
            )
        }

        "Red" -> {
            ThemeColors(
                RedBackground,
                RedAccent,
                RedSnake,
                RedAccent.copy(alpha = 0.2f),
                RedBackground.copy(alpha = 0.8f),
            )
        }

        "Yellow" -> {
            ThemeColors(
                YellowBackground,
                YellowAccent,
                YellowSnake,
                YellowAccent.copy(alpha = 0.2f),
                YellowBackground.copy(alpha = 0.8f),
            )
        }

        "Orange" -> {
            ThemeColors(
                OrangeBackground,
                OrangeAccent,
                OrangeSnake,
                OrangeAccent.copy(alpha = 0.2f),
                OrangeBackground.copy(alpha = 0.8f),
            )
        }

        "Black and White" -> {
            ThemeColors(
                BWBackground,
                BWAccent,
                BWSnake,
                BWAccent.copy(alpha = 0.2f),
                BWBackground.copy(alpha = 0.8f),
            )
        }

        else -> {
            ThemeColors(
                DarkBackground,
                AccentBlue,
                SnakeColor,
                TopBarButtonBackground,
                BottomBarBackground,
            )
        }
    }

@Composable
fun ArrowsTheme(
    themeName: String = "Dark",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val themeColors = getThemeColors(themeName)
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && themeName == "Dark" -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme || themeName != "Light" -> {
                DarkColorScheme.copy(
                    background = themeColors.background,
                    surface = themeColors.background,
                    primary = themeColors.accent,
                )
            }

            else -> {
                LightColorScheme
            }
        }
    CompositionLocalProvider(LocalThemeColors provides themeColors) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
