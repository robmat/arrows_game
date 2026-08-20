package com.batodev.arrows.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.ui.theme.White

/**
 * Round back-arrow icon button used on top bars across features.
 */
@Composable
fun BackIconButton(
    onClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(containerColor = containerColor),
        modifier = modifier.size(40.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.content_description_back),
            tint = White
        )
    }
}
