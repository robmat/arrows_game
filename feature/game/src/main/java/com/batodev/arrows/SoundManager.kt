package com.batodev.arrows

import android.content.Context
import android.content.res.Resources
import android.media.MediaPlayer
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.batodev.arrows.core.resources.R
import kotlin.random.Random

class SoundManager(
    context: Context,
) {
    // Playback is posted to a long-lived background thread, which must not keep an Activity alive.
    private val context = context.applicationContext
    private var isSoundsEnabled = true

    private val switchSounds =
        listOf(
            R.raw.switch1,
            R.raw.switch2,
            R.raw.switch3,
            R.raw.switch4,
            R.raw.switch5,
            R.raw.switch6,
            R.raw.switch7,
            R.raw.switch8,
            R.raw.switch9,
            R.raw.switch10,
            R.raw.switch11,
            R.raw.switch12,
            R.raw.switch13,
            R.raw.switch14,
            R.raw.switch15,
            R.raw.switch16,
            R.raw.switch17,
            R.raw.switch18,
            R.raw.switch19,
            R.raw.switch20,
            R.raw.switch21,
            R.raw.switch22,
            R.raw.switch23,
            R.raw.switch24,
            R.raw.switch25,
            R.raw.switch26,
            R.raw.switch27,
            R.raw.switch28,
            R.raw.switch29,
            R.raw.switch30,
            R.raw.switch31,
            R.raw.switch32,
            R.raw.switch33,
            R.raw.switch34,
            R.raw.switch35,
            R.raw.switch36,
            R.raw.switch37,
            R.raw.switch38,
        )

    fun setSoundsEnabled(enabled: Boolean) {
        isSoundsEnabled = enabled
    }

    fun playRandomSwitch() {
        if (!isSoundsEnabled) return
        val soundId = switchSounds[Random.nextInt(switchSounds.size)]
        playSound(soundId)
    }

    fun playSnakeRemoved() {
        if (!isSoundsEnabled) return
        playSound(R.raw.snake_removed)
    }

    fun playLiveLost() {
        if (!isSoundsEnabled) return
        playSound(R.raw.live_lost)
    }

    fun playGameWon() {
        if (!isSoundsEnabled) return
        playSound(R.raw.game_won)
    }

    fun playGameLost() {
        if (!isSoundsEnabled) return
        playSound(R.raw.game_lost)
    }

    private fun playSound(resId: Int) {
        // MediaPlayer.create() prepares synchronously - binder calls into the media server that can
        // stall on low-end devices - so it must not run inside tap handling on the main thread.
        soundHandler.post {
            val player = createPlayer(resId) ?: return@post
            activePlayers += player
            player.setOnCompletionListener { mp ->
                activePlayers -= mp
                mp.release()
            }
            try {
                player.start()
            } catch (e: IllegalStateException) {
                Log.e("SoundManager", "Failed to play sound: Illegal state", e)
                activePlayers -= player
                player.release()
            }
        }
    }

    private fun createPlayer(resId: Int): MediaPlayer? =
        try {
            MediaPlayer.create(context, resId)
        } catch (e: IllegalStateException) {
            Log.e("SoundManager", "Failed to create sound player: Illegal state", e)
            null
        } catch (e: Resources.NotFoundException) {
            Log.e("SoundManager", "Failed to play sound: Resource not found", e)
            null
        }

    private companion object {
        // Shared by every SoundManager and started on first use: a new SoundManager is built each
        // time the game screen builds its engine factory, so none of them can own a thread.
        // Players created here deliver their completion callbacks on this thread too.
        val soundHandler by lazy { Handler(HandlerThread("SoundManager").apply { start() }.looper) }

        // Only touched on the sound thread. Holds each player until it finishes: nothing else
        // references it after create(), so it could otherwise be finalized mid-sound.
        val activePlayers = mutableSetOf<MediaPlayer>()
    }
}
