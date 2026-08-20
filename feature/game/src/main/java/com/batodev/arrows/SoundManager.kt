package com.batodev.arrows

import android.content.Context
import android.content.res.Resources
import android.media.MediaPlayer
import android.util.Log
import com.batodev.arrows.core.resources.R
import kotlin.random.Random

class SoundManager(
    private val context: Context,
) {
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
        try {
            MediaPlayer.create(context, resId)?.apply {
                setOnCompletionListener { mp ->
                    mp.release()
                }
                start()
            }
        } catch (e: IllegalStateException) {
            Log.e("SoundManager", "Failed to play sound: Illegal state", e)
        } catch (e: Resources.NotFoundException) {
            Log.e("SoundManager", "Failed to play sound: Resource not found", e)
        }
    }
}
