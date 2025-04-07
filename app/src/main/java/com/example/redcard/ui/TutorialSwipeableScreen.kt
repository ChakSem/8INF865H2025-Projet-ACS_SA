package com.example.redcard.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.redcard.R
import com.example.redcard.ui.theme.AppTheme
import com.example.redcard.ui.theme.RedCardTheme
import com.example.redcard.ui.theme.ThemeViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialSwipeableScreen(navController: NavController, themeViewModel: ThemeViewModel) {
    val pagerState = rememberPagerState(pageCount = { 8 }) // Nombre de pages

    // Obtenez le thème actuel via ThemeViewModel
    val currentTheme by themeViewModel.theme.collectAsState()

    // Déterminez si le thème est sombre ou clair
    val darkTheme = when (currentTheme) {
        AppTheme.SOMBRE -> true
        AppTheme.CLAIR -> false
        AppTheme.SYSTEME -> isSystemInDarkTheme()
    }

    // Appliquez le thème (clair ou sombre) en utilisant RedCardTheme
    RedCardTheme(darkTheme = darkTheme) {
        // Toute la colonne et son contenu respectent le thème
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Utilisation de la couleur de fond du thème
        ) {
            // Barre supérieure avec la croix pour fermer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16756C))
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(onClick = { navController.navigate("startingPage") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = MaterialTheme.colorScheme.onBackground // Utilisation de la couleur du thème pour l'icône
                    )
                }
            }

            // HorizontalPager pour swiper entre les pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> FirstScreen()
                    1 -> SecondScreen()
                    2 -> ThirdScreen()
                    3 -> FourthScreen()
                    4 -> FifthScreen()
                    5 -> SixthScreen()
                    6 -> SeventhScreen()
                    7 -> EighthScreen()
                }
            }

            // Indicateurs de pages (points)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(8) { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (pagerState.currentPage == index) Color.Red else Color.Gray)
                    )
                }
            }
        }
    }
}

// Écrans de chaque page
@Composable
fun FirstScreen() {
    GenericPage(
        imageResId = R.drawable.redcard_logo,
        title = "Bienvenue !",
        subtitle = "Red Card est une jeu de société d'ambiance " +
                "et de bluff qui se joue avec des mots entre amis"
    )
}



@Composable
fun SecondScreen() {
    GenericPage(
        imageResId = R.drawable.redcard_logo,
        title = "Les Titulaires",
        subtitle = "Ils ont le même mot secret. Ils doivent trouver qui sont les Remplaçants et les Footix !"
    )
}

@Composable
fun ThirdScreen() {
    GenericPage(
        imageResId = R.drawable.redcard_logo,
        title = "Le remplaçant",
        subtitle = "Il a un mot légèrement différent, son but est de ne pas se faire démasquer par les Titulaires !"
    )
}

@Composable
fun FourthScreen() {
    GenericPage(
        imageResId = R.drawable.redcard_logo,
        title = "Le Footix",
        subtitle = "Il n'a pas de mot secret. Il doit feindre d'en avoir un en utilisant son talent " +
                "pour bluffer et convaincre les autres qu'il s'y connaît en football, un véritable Footix ! " +
                "S'il est éliminé, il a la possibilité de deviner le mot secret. S'il trouve le bon mot, il gagne !"
    )
}

@Composable
fun FifthScreen() {
    GenericPage(
        imageResId = R.drawable.redcard_logo,
        title = "Obtenez votre mot",
        subtitle = "Passez le téléphone d'un joueur à l'autre pour obtenir votre mot secret !"
    )
}

@Composable
fun SixthScreen() {
    GenericPage(
        imageResId = R.drawable.redcard_logo,
        title = "Devinez votre mot",
        subtitle = "Chacun décrit son mot secret, et le Footix doit improviser comme toujours !"
    )
}

@Composable
fun SeventhScreen() {
    GenericPage(
        imageResId = R.drawable.redcard_logo,
        title = "Discutez",
        subtitle = "Discutez entre vous pour démasquer le/les remplaçants et le Footix !"
    )
}

@Composable
fun EighthScreen() {
    GenericPage(
        imageResId = R.drawable.redcard_logo,
        title = "Votez",
        subtitle = "Votez en pointant du doigt en même temps que les autres joueurs restants pour savoir qui sera éliminé !"
    )
}

@Composable
fun GenericPage(
    imageResId: Int,
    title: String,
    subtitle: String
) {
    val themeViewModel = remember { ThemeViewModel() }
    val currentTheme by themeViewModel.theme.collectAsState()
    val darkTheme = when (currentTheme) {
        AppTheme.SOMBRE -> true
        AppTheme.CLAIR -> false
        AppTheme.SYSTEME -> isSystemInDarkTheme()
    }

    val backgroundColor = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Partie supérieure avec forme diagonale
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // hauteur totale de la zone du quadrilatère
        ) {
            // Canvas avec forme quadrilatère
            Canvas(modifier = Modifier.matchParentSize()) {
                val path = Path().apply {
                    moveTo(0f, 0f) // haut gauche
                    lineTo(size.width, 0f) // haut droit
                    lineTo(size.width, size.height * 0.85f) // bas droit (plus haut que la gauche)
                    lineTo(0f, size.height) // bas gauche
                    close()
                }
                drawPath(path = path, color = Color(0xFF16756C))
            }

            // Logo centré dans la zone
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(250.dp)
                        .height(250.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Titre (centré)
        Text(
            text = title,
            fontFamily = FontFamily(Font(R.font.rubik_bold)),
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sous-texte (aligné à gauche)
        Text(
            text = subtitle,
            fontFamily = FontFamily(Font(R.font.rubik_regular)),
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .align(Alignment.Start)
        )
    }
}

