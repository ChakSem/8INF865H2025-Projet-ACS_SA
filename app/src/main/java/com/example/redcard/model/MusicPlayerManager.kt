package com.example.redcard.model

import android.content.Context
import android.media.MediaPlayer
import com.example.redcard.R

object MusicPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var isInitialized = false
    private var currentResId: Int? = null // ➕ ajouté
    var isPlaying = true

    fun playMusicIntro(context: Context) {
        startNewMusic(context, R.raw.intro)
    }

    fun playMusicSalon(context: Context) {
        startNewMusic(context, R.raw.salon)
    }

    fun playMusicGame(context: Context) {
        startNewMusic(context, R.raw.game)
    }

    private fun startNewMusic(context: Context, resId: Int) {
        if (mediaPlayer == null || currentResId != resId) {
            stopMusic()
            mediaPlayer = MediaPlayer.create(context, resId).apply {
                isLooping = true
                start()
            }
            currentResId = resId
            isInitialized = true
            isPlaying = true
        } else if (!mediaPlayer!!.isPlaying) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    fun pauseMusic() {
        mediaPlayer?.pause()
        isPlaying = false
    }

    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isInitialized = false
        isPlaying = false
        currentResId = null // ➕ réinitialiser
    }

    fun toggleMusic(enabled: Boolean, context: Context, musicToPlay: () -> Unit) {
        if (enabled) {
            if (!isPlaying) {
                musicToPlay()
            }
        } else {
            pauseMusic()
        }
    }

    fun resumeMusic() {
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

}



