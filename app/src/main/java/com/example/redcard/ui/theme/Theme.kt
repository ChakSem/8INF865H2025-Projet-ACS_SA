package com.example.redcard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Définir les couleurs pour le thème sombre
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF0000),  // Exemple de couleur primaire pour le thème sombre
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    background = Color(0xFF1F2936),  // Bleu-gris foncé pour le fond du thème sombre
    onBackground = Color.White,  // Texte blanc
    surface = Color(0xFF1F2936),  // Bleu-gris foncé pour la surface
    onSurface = Color.White,  // Texte blanc pour la surface
)

// Définir les couleurs pour le thème clair
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF0000),  // Exemple de couleur primaire pour le thème clair
    onPrimary = Color.White,
    secondary = Color(0xFF16756C),
    onSecondary = Color.Black,
    background = Color(0xFFFFFFFF),  // Bleu-verdâtre clair pour le fond
    onBackground = Color.Black,  // Texte noir
    surface = Color(0xFFFFFFFF),  // Bleu-verdâtre clair pour la surface
    onSurface = Color.Black,  // Texte noir pour la surface
)

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun RedCardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Définir la couleur des icônes en fonction du thème
    val iconColor = if (darkTheme) Color.White else Color.Black

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,  // Utilisation des formes définies ci-dessus
        content = {
            // Appliquer la couleur des icônes aux composants de l'application
            CompositionLocalProvider(
                LocalContentColor provides iconColor
            ) {
                content()
            }
        }
    )
}
