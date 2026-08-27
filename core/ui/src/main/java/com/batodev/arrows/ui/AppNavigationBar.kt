package com.batodev.arrows.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.batodev.arrows.GameConstants
import com.batodev.arrows.core.ui.BuildConfig
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.ui.theme.InactiveIcon
import com.batodev.arrows.ui.theme.NavigationIndicator
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White

@Composable
fun AppNavigationBar(
    selectedDestination: NavigationDestination,
    levelNumber: Int,
    themeColors: ThemeColors,
    modifier: Modifier = Modifier,
    onNavigateHome: () -> Unit = {},
    onNavigateToGenerate: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    val isGeneratorUnlocked = levelNumber >= GameConstants.GENERATOR_UNLOCK_LEVEL

    NavigationBar(
        containerColor = themeColors.bottomBar,
        contentColor = White,
        modifier = modifier,
    ) {
        GeneratorNavigationItem(
            isUnlocked = isGeneratorUnlocked,
            selected = selectedDestination == NavigationDestination.GENERATOR,
            onNavigate = onNavigateToGenerate,
        )
        HomeNavigationItem(
            selected = selectedDestination == NavigationDestination.HOME,
            onNavigate = onNavigateHome,
        )
        SettingsNavigationItem(
            selected = selectedDestination == NavigationDestination.SETTINGS,
            onNavigate = onNavigateToSettings,
        )
    }
}

@Composable
private fun RowScope.SimpleNavigationItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onNavigate: () -> Unit,
) {
    NavigationBarItem(
        icon = {
            Icon(
                icon,
                contentDescription = label,
            )
        },
        label = { Text(label) },
        selected = selected,
        onClick = {
            if (!selected) {
                onNavigate()
            }
        },
        colors =
            NavigationBarItemDefaults.colors(
                selectedIconColor = White,
                indicatorColor = NavigationIndicator,
                selectedTextColor = White,
                unselectedIconColor = InactiveIcon,
                unselectedTextColor = InactiveIcon,
            ),
    )
}

@Composable
fun RowScope.HomeNavigationItem(
    selected: Boolean,
    onNavigate: () -> Unit,
) {
    SimpleNavigationItem(
        icon = Icons.Default.Home,
        label = stringResource(R.string.home_label),
        selected = selected,
        onNavigate = onNavigate,
    )
}

@Composable
fun RowScope.SettingsNavigationItem(
    selected: Boolean,
    onNavigate: () -> Unit,
) {
    SimpleNavigationItem(
        icon = Icons.Default.Settings,
        label = stringResource(R.string.settings_label),
        selected = selected,
        onNavigate = onNavigate,
    )
}

@Composable
fun RowScope.GeneratorNavigationItem(
    isUnlocked: Boolean,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    // Debug builds keep the locked icon/label below GENERATOR_UNLOCK_LEVEL, but stay
    // navigable, so a developer can reach the generator without playing to level 20.
    val canNavigate = isUnlocked || BuildConfig.DEBUG
    val icon = if (isUnlocked) Icons.Default.AutoAwesome else Icons.Default.Lock
    val label =
        if (isUnlocked) {
            stringResource(R.string.custom_gen_title)
        } else {
            stringResource(R.string.level_label, GameConstants.GENERATOR_UNLOCK_LEVEL)
        }

    NavigationBarItem(
        icon = { Icon(icon, contentDescription = stringResource(R.string.content_description_generate)) },
        label = { Text(label) },
        selected = selected,
        onClick = {
            if (canNavigate && !selected) {
                onNavigate()
            }
        },
        modifier = modifier,
        colors =
            NavigationBarItemDefaults.colors(
                selectedIconColor = White,
                unselectedIconColor = InactiveIcon,
                selectedTextColor = White,
                unselectedTextColor = InactiveIcon,
                indicatorColor = NavigationIndicator,
            ),
    )
}
