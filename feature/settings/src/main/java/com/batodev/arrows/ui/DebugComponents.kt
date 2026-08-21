package com.batodev.arrows.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.engine.DebugDialogParams
import com.batodev.arrows.ui.theme.InactiveIcon
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.White

data class DebugMenuState(
    val levelNumber: Int,
    val forcedWidth: Int?,
    val forcedHeight: Int?,
    val forcedLives: Int?,
    val forcedShape: String?,
    val shapes: List<String?>,
    val onRegenerateLevel: () -> Unit,
    val onSaveLevelNumber: (Int) -> Unit,
    val onSaveDebugOption: (DebugViewModel.DebugOption, Any?) -> Unit,
)

@Composable
fun DebugMenu(
    state: DebugMenuState,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current

    var dialogToShow by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier) {
        Text(
            stringResource(R.string.debug_menu_title),
            color = themeColors.accent,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )

        SettingsGroup(themeColors.topBarButton) {
            SettingsClickableItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.current_level_label),
                valueText = state.levelNumber.toString(),
                onClick = { dialogToShow = "level" },
            )
            SettingsClickableItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.forced_width_label),
                valueText = state.forcedWidth?.toString() ?: stringResource(R.string.auto_label),
                onClick = { dialogToShow = "width" },
            )
            SettingsClickableItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.forced_height_label),
                valueText = state.forcedHeight?.toString() ?: stringResource(R.string.auto_label),
                onClick = { dialogToShow = "height" },
            )
            SettingsClickableItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.forced_lives_label),
                valueText = state.forcedLives?.toString() ?: stringResource(R.string.auto_label),
                onClick = { dialogToShow = "lives" },
            )
            SettingsClickableItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.forced_shape_label),
                valueText = state.forcedShape ?: stringResource(R.string.none_label),
                onClick = { dialogToShow = "shape" },
            )
            SettingsClickableItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.regenerate_level_label),
                onClick = { state.onRegenerateLevel() },
            )
        }
    }

    DebugDialogs(
        shapes = state.shapes,
        onSaveLevelNumber = state.onSaveLevelNumber,
        onSaveDebugOption = state.onSaveDebugOption,
        params =
            DebugDialogParams(
                dialogToShow,
                state.levelNumber,
                state.forcedWidth,
                state.forcedHeight,
                state.forcedLives,
                state.forcedShape,
            ) {
                dialogToShow = null
            },
    )
}

@Composable
private fun DebugDialogs(
    shapes: List<String?>,
    onSaveLevelNumber: (Int) -> Unit,
    onSaveDebugOption: (DebugViewModel.DebugOption, Any?) -> Unit,
    params: DebugDialogParams,
) {
    when (params.dialogToShow) {
        "level" -> {
            NumberInputDialog(
                stringResource(R.string.level_dialog_title),
                params.levelNumber,
                params.onDismiss,
            ) {
                onSaveLevelNumber(it)
            }
        }

        "width" -> {
            NumberInputDialog(
                stringResource(R.string.width_auto_label),
                params.forcedWidth ?: 0,
                params.onDismiss,
            ) {
                onSaveDebugOption(DebugViewModel.DebugOption.WIDTH, if (it > 0) it else null)
            }
        }

        "height" -> {
            NumberInputDialog(
                stringResource(R.string.height_auto_label),
                params.forcedHeight ?: 0,
                params.onDismiss,
            ) {
                onSaveDebugOption(DebugViewModel.DebugOption.HEIGHT, if (it > 0) it else null)
            }
        }

        "lives" -> {
            NumberInputDialog(
                stringResource(R.string.lives_auto_label),
                params.forcedLives ?: 0,
                params.onDismiss,
            ) {
                onSaveDebugOption(DebugViewModel.DebugOption.LIVES, if (it > 0) it else null)
            }
        }

        "shape" -> {
            ShapeSelectionDialog(shapes, params.forcedShape, params.onDismiss) {
                onSaveDebugOption(DebugViewModel.DebugOption.SHAPE, it)
            }
        }
    }
}

@Composable
fun NumberInputDialog(
    title: String,
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var text by remember { mutableStateOf(initialValue.toString()) }
    val themeColors = LocalThemeColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = themeColors.bottomBar,
        title = { Text(title, color = White) },
        text = {
            androidx.compose.material3.TextField(
                value = text,
                onValueChange = { if (it.all { c -> c.isDigit() }) text = it },
                keyboardOptions =
                    androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    ),
                colors =
                    androidx.compose.material3.TextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
            )
        },
        confirmButton = {
            TextButton(onClick = {
                text.toIntOrNull()?.let { onConfirm(it) }
                onDismiss()
            }) {
                Text(stringResource(R.string.ok_label), color = themeColors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_label), color = themeColors.accent)
            }
        },
    )
}

@Composable
fun ShapeSelectionDialog(
    shapes: List<String?>,
    currentShape: String?,
    onDismiss: () -> Unit,
    onShapeSelect: (String?) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = themeColors.bottomBar,
        title = { Text(stringResource(R.string.choose_forced_shape_title), color = White) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                shapes.forEach { shape ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onShapeSelect(shape)
                                    onDismiss()
                                }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = shape == currentShape,
                            onClick = {
                                onShapeSelect(shape)
                                onDismiss()
                            },
                            colors =
                                RadioButtonDefaults.colors(
                                    selectedColor = themeColors.accent,
                                    unselectedColor = InactiveIcon,
                                ),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = shape ?: stringResource(R.string.none_auto_label),
                            color = White,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_label), color = themeColors.accent)
            }
        },
    )
}
