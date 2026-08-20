package com.batodev.arrows.ui.game

import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.batodev.arrows.GameConstants
import kotlinx.coroutines.delay

@Composable
fun WinCelebrationScreen(onCelebrationComplete: () -> Unit) {
    var videoAlpha by remember { mutableFloatStateOf(0f) }
    var shouldShowCelebration by remember { mutableStateOf(true) }
    val celebration = remember { CelebrationContentSelector.selectContent() }

    val animatedAlpha by animateFloatAsState(
        targetValue = videoAlpha,
        animationSpec = tween(durationMillis = GameConstants.VIDEO_FADE_IN_DURATION),
        label = "video_fade",
    )

    PlayCelebrationTimeline(
        onFadeInStart = { videoAlpha = 1f },
        onFadeOutStart = { videoAlpha = 0f },
        onComplete = {
            shouldShowCelebration = false
            onCelebrationComplete()
        },
    )

    if (shouldShowCelebration) {
        CelebrationContent(celebration.videoResId, celebration.labelResId, animatedAlpha)
    }
}

@Composable
private fun PlayCelebrationTimeline(
    onFadeInStart: () -> Unit,
    onFadeOutStart: () -> Unit,
    onComplete: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(GameConstants.VIDEO_PREPARATION_DELAY)
        onFadeInStart()
        delay(GameConstants.VIDEO_FADE_IN_DURATION.toLong())
        delay(GameConstants.VIDEO_DISPLAY_DURATION.toLong())
        onFadeOutStart()
        delay(GameConstants.VIDEO_FADE_OUT_DURATION.toLong())
        onComplete()
    }
}

@Composable
private fun CelebrationContent(
    videoResId: Int,
    labelResId: Int,
    alpha: Float,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        // Video plays underneath
        VideoPlayerView(
            videoResId = videoResId,
            modifier = Modifier.fillMaxSize(),
        )
        // Black overlay fades out (1-alpha) for fade-in, fades in for fade-out
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .alpha(1f - alpha)
                    .background(Color.Black),
        )
        // Text fades with the content
        Text(
            text = stringResource(labelResId),
            fontSize = GameConstants.CONGRATULATIONS_FONT_SIZE.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.alpha(alpha),
        )
    }
}

@Composable
private fun VideoPlayerView(
    videoResId: Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            TextureView(ctx).apply {
                surfaceTextureListener = createSurfaceTextureListener(mediaPlayer, ctx, videoResId)
            }
        },
        modifier = modifier,
    )
}

private fun createSurfaceTextureListener(
    mediaPlayer: MediaPlayer,
    context: android.content.Context,
    videoResId: Int,
): TextureView.SurfaceTextureListener =
    object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(
            surfaceTexture: SurfaceTexture,
            width: Int,
            height: Int,
        ) {
            prepareAndStartVideo(mediaPlayer, context, videoResId, Surface(surfaceTexture))
        }

        override fun onSurfaceTextureSizeChanged(
            surfaceTexture: SurfaceTexture,
            width: Int,
            height: Int,
        ) = Unit

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean = true

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
    }

private fun prepareAndStartVideo(
    mediaPlayer: MediaPlayer,
    context: android.content.Context,
    videoResId: Int,
    surface: Surface,
) {
    try {
        mediaPlayer.reset()
        mediaPlayer.setSurface(surface)

        // Configure audio attributes to NOT request audio focus
        // This prevents YouTube and other media apps from pausing
        val audioAttributes =
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .build()
        mediaPlayer.setAudioAttributes(audioAttributes)

        // Mute the video completely
        mediaPlayer.setVolume(0f, 0f)

        val afd = context.resources.openRawResourceFd(videoResId)
        mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()

        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener { mp ->
            mp.start()
        }
    } catch (e: java.io.IOException) {
        android.util.Log.w("WinCelebration", "Failed to play celebration video", e)
    } catch (e: IllegalStateException) {
        android.util.Log.w("WinCelebration", "MediaPlayer in invalid state", e)
    }
}
