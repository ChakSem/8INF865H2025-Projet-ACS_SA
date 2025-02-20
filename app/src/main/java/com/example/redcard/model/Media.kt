package com.example.redcard.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

// Modèle représentant un média (image, icône, gif)
data class  Media(
    @StringRes val description: Int, // Description textuelle de l'élément (ex. : titre ou texte alternatif)
    @DrawableRes val imageResId: Int, // ID de la ressource image (logo, icône, etc.)
    val isGif: Boolean = false // Indicateur pour savoir si c'est un GIF
)
