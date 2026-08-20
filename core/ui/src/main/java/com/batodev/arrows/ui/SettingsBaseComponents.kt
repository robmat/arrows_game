package com.batodev.arrows.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.ui.theme.InactiveIcon
import com.batodev.arrows.ui.theme.White

@Composable
fun SettingsGroup(backgroundColor: Color, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(vertical = 8.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    rowModifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(rowModifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        trailingContent()
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    initialValue: Boolean,
    accentColor: Color,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    var checked by remember { mutableStateOf(initialValue) }
    LaunchedEffect(initialValue) { checked = initialValue }
    SettingsItemRow(icon = icon, title = title) {
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChange?.invoke(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = White,
                uncheckedTrackColor = InactiveIcon
            )
        )
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    valueText: String? = null,
    onClick: () -> Unit
) {
    SettingsItemRow(icon = icon, title = title, rowModifier = Modifier.clickable(onClick = onClick)) {
        if (valueText != null) {
            Text(
                text = valueText,
                color = White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = stringResource(R.string.content_description_open),
            tint = White.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}
