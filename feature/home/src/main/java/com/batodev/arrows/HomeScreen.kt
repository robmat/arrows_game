package com.batodev.arrows

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.ui.AppNavigationBar
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.NavigationDestination
import com.batodev.arrows.ui.ads.BannerAdView
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White

@Composable
fun MainScreen(
    appViewModel: AppViewModel,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToGenerate: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    val hasSavedLevel by appViewModel.hasSavedLevel.collectAsState()
    val levelNumber by appViewModel.levelNumber.collectAsState()
    val isAdFree by appViewModel.isAdFree.collectAsState()
    val themeColors = LocalThemeColors.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        modifier = modifier,
        containerColor = themeColors.background,
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!isAdFree) {
                    BannerAdView()
                }
                AppNavigationBar(
                    selectedDestination = NavigationDestination.HOME,
                    levelNumber = levelNumber,
                    themeColors = themeColors,
                    onNavigateHome = {},
                    onNavigateToGenerate = onNavigateToGenerate,
                    onNavigateToSettings = onNavigateToSettings,
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            LogoSection(levelNumber, themeColors, visible)
            Spacer(modifier = Modifier.weight(1f))
            PlayButton(hasSavedLevel, themeColors, visible, onPlay)
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun Modifier.homeEntryModifier(
    visible: Boolean,
    staggerIndex: Int,
): Modifier {
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else GameConstants.HOME_ENTER_OFFSET_DP,
        animationSpec =
            tween(
                durationMillis = GameConstants.HOME_ENTER_ANIM_DURATION,
                delayMillis = HomeScreenLogic.entryDelayMs(staggerIndex),
            ),
        label = "homeEntryTransY$staggerIndex",
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = GameConstants.HOME_ENTER_ANIM_DURATION,
                delayMillis = HomeScreenLogic.entryDelayMs(staggerIndex),
            ),
        label = "homeEntryAlpha$staggerIndex",
    )
    return graphicsLayer {
        this.translationY = translationY * density
        this.alpha = alpha
    }
}

@Composable
private fun LogoSection(
    levelNumber: Int,
    themeColors: ThemeColors,
    visible: Boolean,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoAnim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = GameConstants.HOME_TRIANGLE_ROTATE_DURATION,
                        easing = LinearEasing,
                    ),
                repeatMode = RepeatMode.Restart,
            ),
        label = "triangleRotation",
    )
    val trianglePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = GameConstants.HOME_TRIANGLE_PULSE_SCALE,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = GameConstants.HOME_TRIANGLE_PULSE_DURATION),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "trianglePulse",
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.homeEntryModifier(visible, staggerIndex = 0),
        ) {
            TriangleIcon(
                modifier =
                    Modifier
                        .size(40.dp)
                        .graphicsLayer {
                            rotationZ = rotation
                            scaleX = trianglePulse
                            scaleY = trianglePulse
                        },
                color = White,
            )
            Text(
                text = stringResource(R.string.logo_text),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = White,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.level_label, levelNumber),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.accent,
            modifier = Modifier.homeEntryModifier(visible, staggerIndex = 1),
        )
    }
}

@Composable
private fun PlayButton(
    isContinue: Boolean,
    themeColors: ThemeColors,
    visible: Boolean,
    onClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "buttonPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = GameConstants.HOME_BUTTON_PULSE_SCALE,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = GameConstants.HOME_BUTTON_PULSE_DURATION),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "buttonPulseScale",
    )
    val text =
        if (isContinue) {
            stringResource(R.string.continue_label)
        } else {
            stringResource(R.string.play_label)
        }
    Button(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .homeEntryModifier(visible, staggerIndex = 2)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                },
        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
        shape = RoundedCornerShape(28.dp),
    ) {
        Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TriangleIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val path =
            Path().apply {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
        drawPath(path = path, color = color)
    }
}
