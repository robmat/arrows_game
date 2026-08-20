package com.batodev.arrows

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.arrows.core.resources.R
import com.batodev.arrows.data.AndroidResourceBoardShapeProvider
import com.batodev.arrows.data.ShapeRegistry
import com.batodev.arrows.ui.AppNavigationBar
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.NavigationDestination
import com.batodev.arrows.ui.ads.BannerAdView
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateScreen(
    appViewModel: AppViewModel,
    onStartCustomGame: (CustomGameParams) -> Unit,
    onBack: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    val context = LocalContext.current
    val hasSavedLevel by appViewModel.hasSavedLevel.collectAsState()
    val isFillBoardEnabled by appViewModel.isFillBoardEnabled.collectAsState()
    val levelNumber by appViewModel.levelNumber.collectAsState()
    val isAdFree by appViewModel.isAdFree.collectAsState()
    val themeColors = LocalThemeColors.current
    val maxSize = GenerateScreenLogic.resolveMaxSize(isFillBoardEnabled)
    var width by remember { mutableFloatStateOf(GameConstants.GENERATOR_DEFAULT_SIZE) }
    var height by remember { mutableFloatStateOf(GameConstants.GENERATOR_DEFAULT_SIZE) }
    var selectedShape by remember { mutableStateOf(GameConstants.SHAPE_TYPE_RECTANGULAR) }
    var showWarning by remember { mutableStateOf(false) }
    var contentReady by remember { mutableStateOf(false) }
    if (width > maxSize) width = maxSize
    if (height > maxSize) height = maxSize
    val shapeProvider = remember { AndroidResourceBoardShapeProvider(context) }
    val shapes = remember { GenerateScreenLogic.buildShapeList(shapeProvider.getAllShapeNames()) }

    LaunchedEffect(Unit) { contentReady = true }

    if (showWarning) {
        WarningDialog(
            themeColors = themeColors,
            onConfirm = { startCustomGameNavigation(appViewModel, onStartCustomGame, width, selectedShape, height) },
            onDismiss = { showWarning = false },
        )
    }
    val scaffoldState =
        GenerateScaffoldState(
            context,
            themeColors,
            levelNumber,
            width,
            height,
            maxSize,
            shapes,
            selectedShape,
            isAdFree,
            contentReady,
            { width = it },
            { height = it },
            { selectedShape = it },
            onBack,
            onNavigateHome,
            onNavigateToSettings,
        ) {
            if (hasSavedLevel) {
                showWarning = true
            } else {
                startCustomGameNavigation(appViewModel, onStartCustomGame, width, selectedShape, height)
            }
        }
    GenerateScaffoldContent(scaffoldState)
}

private data class GenerateScaffoldState(
    val context: android.content.Context,
    val themeColors: ThemeColors,
    val levelNumber: Int,
    val width: Float,
    val height: Float,
    val maxSize: Float,
    val shapes: List<String>,
    val selectedShape: String,
    val isAdFree: Boolean,
    val contentReady: Boolean,
    val onWidthChange: (Float) -> Unit,
    val onHeightChange: (Float) -> Unit,
    val onShapeSelected: (String) -> Unit,
    val onBack: () -> Unit,
    val onNavigateHome: () -> Unit,
    val onNavigateToSettings: () -> Unit,
    val onStartClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenerateScaffoldContent(state: GenerateScaffoldState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.custom_gen_title), color = White) },
                navigationIcon = {
                    IconButton(
                        onClick = state.onBack,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = White),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_description_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = state.themeColors.background),
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!state.isAdFree) {
                    BannerAdView()
                }
                AppNavigationBar(
                    selectedDestination = NavigationDestination.GENERATOR,
                    levelNumber = state.levelNumber,
                    themeColors = state.themeColors,
                    onNavigateHome = state.onNavigateHome,
                    onNavigateToGenerate = {},
                    onNavigateToSettings = state.onNavigateToSettings,
                )
            }
        },
        containerColor = state.themeColors.background,
    ) { innerPadding ->
        if (state.contentReady) {
            val contentState =
                GenerateContentState(
                    innerPadding = innerPadding,
                    width = state.width,
                    height = state.height,
                    maxSize = state.maxSize,
                    shapes = state.shapes,
                    selectedShape = state.selectedShape,
                    themeColors = state.themeColors,
                    onWidthChange = state.onWidthChange,
                    onHeightChange = state.onHeightChange,
                    onShapeSelected = state.onShapeSelected,
                    onStartClick = state.onStartClick,
                )
            GenerateContent(contentState)
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding))
        }
    }
}

private fun startCustomGameNavigation(
    viewModel: AppViewModel,
    onStartCustomGame: (CustomGameParams) -> Unit,
    width: Float,
    selectedShape: String,
    height: Float,
) {
    viewModel.regenerateCurrentLevel()
    onStartCustomGame(GenerateScreenLogic.buildCustomGameParams(width, height, selectedShape))
}

@Composable
private fun WarningDialog(
    themeColors: ThemeColors,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_gen_warning_title), color = White) },
        text = { Text(stringResource(R.string.custom_gen_warning_message), color = White) },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(stringResource(R.string.proceed_label))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_label), color = White)
            }
        },
        containerColor = themeColors.background,
    )
}

private data class GenerateContentState(
    val innerPadding: PaddingValues,
    val width: Float,
    val height: Float,
    val maxSize: Float,
    val shapes: List<String>,
    val selectedShape: String,
    val themeColors: ThemeColors,
    val onWidthChange: (Float) -> Unit,
    val onHeightChange: (Float) -> Unit,
    val onShapeSelected: (String) -> Unit,
    val onStartClick: () -> Unit,
)

@Composable
private fun GenerateContent(state: GenerateContentState) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val widthSliderMod = Modifier.staggeredEntryModifier(visible, staggerIndex = 0)
    val heightSliderMod = Modifier.staggeredEntryModifier(visible, staggerIndex = 1)
    val shapeSectionMod = Modifier.staggeredEntryModifier(visible, staggerIndex = 2)

    val buttonEntryAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = GameConstants.GENERATOR_ENTER_ANIM_DURATION,
                delayMillis = BUTTON_STAGGER_INDEX * GameConstants.GENERATOR_STAGGER_DELAY_MS,
            ),
        label = "button_entry_alpha",
    )
    val buttonEntryOffset by animateFloatAsState(
        targetValue = if (visible) 0f else GameConstants.GENERATOR_ENTER_OFFSET_DP,
        animationSpec =
            tween(
                durationMillis = GameConstants.GENERATOR_ENTER_ANIM_DURATION,
                delayMillis = BUTTON_STAGGER_INDEX * GameConstants.GENERATOR_STAGGER_DELAY_MS,
            ),
        label = "button_entry_offset",
    )
    val infiniteTransition = rememberInfiniteTransition(label = "button_pulse")
    val buttonPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = GameConstants.GENERATOR_BUTTON_PULSE_SCALE,
        animationSpec =
            infiniteRepeatable(
                animation = tween(GameConstants.GENERATOR_BUTTON_PULSE_DURATION, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "button_pulse_scale",
    )

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(state.innerPadding)
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SizeSlider(
                label = stringResource(R.string.width_label),
                value = state.width,
                maxSize = state.maxSize,
                onValueChange = state.onWidthChange,
                themeColors = state.themeColors,
                modifier = widthSliderMod,
            )
            Spacer(modifier = Modifier.height(24.dp))
            SizeSlider(
                label = stringResource(R.string.height_label),
                value = state.height,
                maxSize = state.maxSize,
                onValueChange = state.onHeightChange,
                themeColors = state.themeColors,
                modifier = heightSliderMod,
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Column(modifier = shapeSectionMod.fillMaxWidth()) {
                ShapeSectionHeader()
                Spacer(modifier = Modifier.height(12.dp))
                state.shapes.chunked(SHAPES_PER_ROW).forEachIndexed { rowIndex, row ->
                    ShapeRow(
                        shapes = row,
                        rowIndex = rowIndex,
                        selectedShape = state.selectedShape,
                        themeColors = state.themeColors,
                        onShapeSelected = state.onShapeSelected,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = state.onStartClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .offset { IntOffset(0, buttonEntryOffset.dp.roundToPx()) }
                        .graphicsLayer {
                            alpha = buttonEntryAlpha
                            scaleX = buttonPulseScale
                            scaleY = buttonPulseScale
                        },
                colors = ButtonDefaults.buttonColors(containerColor = state.themeColors.accent),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.generate_start_label),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private const val SHAPES_PER_ROW = 4
private const val BUTTON_STAGGER_INDEX = 3

@Composable
private fun Modifier.staggeredEntryModifier(
    visible: Boolean,
    staggerIndex: Int,
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = GameConstants.GENERATOR_ENTER_ANIM_DURATION,
                delayMillis = staggerIndex * GameConstants.GENERATOR_STAGGER_DELAY_MS,
            ),
        label = "entry_alpha_$staggerIndex",
    )
    val offsetDp by animateFloatAsState(
        targetValue = if (visible) 0f else GameConstants.GENERATOR_ENTER_OFFSET_DP,
        animationSpec =
            tween(
                durationMillis = GameConstants.GENERATOR_ENTER_ANIM_DURATION,
                delayMillis = staggerIndex * GameConstants.GENERATOR_STAGGER_DELAY_MS,
            ),
        label = "entry_offset_$staggerIndex",
    )
    return this
        .offset { IntOffset(0, offsetDp.dp.roundToPx()) }
        .graphicsLayer { this.alpha = alpha }
}

@Composable
private fun ShapeSectionHeader() {
    Text(
        text = stringResource(R.string.shape_label),
        color = White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ShapeRow(
    shapes: List<String>,
    rowIndex: Int,
    selectedShape: String,
    themeColors: ThemeColors,
    onShapeSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    ) {
        shapes.forEachIndexed { indexInRow, shape ->
            ShapeItem(
                name = shape,
                globalIndex = GenerateScreenLogic.shapeFlatIndex(rowIndex, indexInRow, SHAPES_PER_ROW),
                isSelected = selectedShape == shape,
                onClick = { onShapeSelected(shape) },
                themeColors = themeColors,
            )
        }
    }
}

@Composable
private fun SizeSlider(
    label: String,
    value: Float,
    maxSize: Float,
    onValueChange: (Float) -> Unit,
    themeColors: ThemeColors,
    modifier: Modifier = Modifier,
) {
    val intValue = value.toInt()
    var scaleUp by remember { mutableStateOf(false) }
    LaunchedEffect(intValue) {
        scaleUp = true
        delay(GameConstants.GENERATOR_VALUE_SCALE_HOLD_MS)
        scaleUp = false
    }
    val valueTextScale by animateFloatAsState(
        targetValue = if (scaleUp) GameConstants.GENERATOR_VALUE_SCALE_TARGET else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh,
            ),
        label = "value_scale",
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, color = White, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = intValue.toString(),
                color = themeColors.accent,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier =
                    Modifier.graphicsLayer {
                        scaleX = valueTextScale
                        scaleY = valueTextScale
                    },
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = GameConstants.GENERATOR_MIN_SIZE..maxSize,
            modifier = Modifier.padding(horizontal = 12.dp),
            colors =
                SliderDefaults.colors(
                    thumbColor = themeColors.accent,
                    activeTrackColor = themeColors.accent,
                    inactiveTrackColor = themeColors.topBarButton,
                ),
        )
    }
}

@Composable
private fun ShapeItem(
    name: String,
    globalIndex: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    themeColors: ThemeColors,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) themeColors.accent else themeColors.topBarButton,
        animationSpec = tween(durationMillis = GameConstants.GENERATOR_COLOR_ANIM_DURATION),
        label = "shape_color",
    )
    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) GameConstants.GENERATOR_SHAPE_SELECTED_SCALE else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        label = "shape_selection_scale",
    )
    var hasPopped by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(GenerateScreenLogic.shapePopInDelayMs(globalIndex))
        hasPopped = true
    }
    val popInScale by animateFloatAsState(
        targetValue = if (hasPopped) 1f else 0f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "shape_pop_in_scale",
    )
    Card(
        modifier =
            Modifier
                .graphicsLayer {
                    val combined = popInScale * selectionScale
                    scaleX = combined
                    scaleY = combined
                }.clickable { onClick() },
        colors =
            CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = White,
            ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            ShapeIcon(name)
        }
    }
}

@Composable
private fun ShapeIcon(name: String) {
    if (name == GameConstants.SHAPE_TYPE_RECTANGULAR) {
        Icon(
            imageVector = Icons.Default.CropSquare,
            contentDescription = stringResource(R.string.shape_rectangular),
            modifier = Modifier.size(GameConstants.SHAPE_ICON_SIZE.dp),
        )
    } else {
        val resId = getShapeResourceId(name)
        if (resId != null) {
            Icon(
                painter = painterResource(id = resId),
                contentDescription = name,
                modifier = Modifier.size(GameConstants.SHAPE_ICON_SIZE.dp),
            )
        } else {
            Text(text = name.take(1).uppercase())
        }
    }
}

private fun getShapeResourceId(name: String): Int? = ShapeRegistry.shapes[name]
