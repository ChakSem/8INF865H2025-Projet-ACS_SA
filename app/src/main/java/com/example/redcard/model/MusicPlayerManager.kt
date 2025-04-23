package com.example.redcard.model

import android.content.Context
import android.media.MediaPlayer
import com.example.redcard.R


/**
 * Singleton gérant la lecture de la musique dans l'application.
 * Permet de jouer, mettre en pause, et arrêter différentes pistes sonores.
 */
object MusicPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var isInitialized = false
    private var currentResId: Int? = null
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

    fun playMusicVictory(context: Context) {
        stopMusic()
        mediaPlayer = MediaPlayer.create(context, R.raw.victory).apply {
            isLooping = false
            start()
            setOnCompletionListener {
                stopMusic()
            }
        }
        currentResId = R.raw.victory
        isInitialized = true
        isPlaying = true
    }

    /**
     * Démarre une nouvelle piste musicale en arrêtant celle en cours si nécessaire.
     * Si la même piste est déjà chargée mais en pause, la reprend.
     */
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
        currentResId = null
    }

    /**
     * Active ou désactive la musique en fonction du paramètre enabled.
     * Si activée, joue la musique spécifiée par musicToPlay.
     */
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