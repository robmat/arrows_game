package com.batodev.arrows.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.util.withContext

@Composable
fun ThirdPartyLicensesDialog(onDismiss: () -> Unit) {
    val themeColors = LocalThemeColors.current
    val context = LocalContext.current
    var libraries by remember { mutableStateOf<List<Library>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val libs = Libs.Builder().withContext(context).build()
        libraries = libs.libraries.sortedBy { it.name.lowercase() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(themeColors.background),
        ) {
            LicensesTopBar(onDismiss, themeColors)
            if (error != null) {
                Text(
                    text = error ?: "",
                    modifier = Modifier.padding(16.dp),
                    color = themeColors.snake,
                )
            }
            LicensesList(libraries, themeColors)
        }
    }
}

@Composable
private fun LicensesTopBar(
    onDismiss: () -> Unit,
    themeColors: ThemeColors,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(themeColors.topBarButton)
                .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackIconButton(onClick = onDismiss, containerColor = themeColors.bottomBar)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.third_party_licenses_label),
            color = White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun LicensesList(
    libraries: List<Library>,
    themeColors: ThemeColors,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        items(libraries, key = { it.uniqueId }) { library ->
            LibraryItem(library)
            HorizontalDivider(
                color = themeColors.topBarButton,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun LibraryItem(library: Library) {
    val themeColors = LocalThemeColors.current

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(themeColors.topBarButton)
                .padding(12.dp),
    ) {
        LibraryHeader(library)
        LibraryDevelopers(library)
        LibraryLicense(library, themeColors)
        LibraryUrl(library, themeColors)
    }
}

@Composable
private fun LibraryHeader(library: Library) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = library.name,
            color = White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        if (library.artifactVersion != null) {
            Text(
                text = library.artifactVersion!!,
                color = White.copy(alpha = 0.6f),
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun LibraryDevelopers(library: Library) {
    val developers = library.developers.mapNotNull { it.name }.joinToString(", ")
    if (developers.isNotEmpty()) {
        Column {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = developers,
                color = White.copy(alpha = 0.7f),
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun LibraryLicense(
    library: Library,
    themeColors: ThemeColors,
) {
    val licenses = library.licenses.joinToString(", ") { it.name }
    if (licenses.isNotEmpty()) {
        Column {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = licenses,
                color = themeColors.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun LibraryUrl(
    library: Library,
    themeColors: ThemeColors,
) {
    val uriHandler = LocalUriHandler.current
    val url = library.website ?: library.scm?.url ?: return
    val annotated =
        buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = themeColors.accent.copy(alpha = 0.8f),
                    textDecoration = TextDecoration.Underline,
                    fontSize = 12.sp,
                ),
            ) {
                append(url)
            }
        }
    Column {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = annotated,
            modifier = Modifier.clickable { uriHandler.openUri(url) },
        )
    }
}
