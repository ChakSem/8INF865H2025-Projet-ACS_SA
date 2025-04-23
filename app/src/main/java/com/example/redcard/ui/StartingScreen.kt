package com.example.redcard.ui


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.redcard.R
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.MenuBook

import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.example.redcard.ui.theme.AppTheme
import com.example.redcard.ui.theme.RedCardTheme
import com.example.redcard.ui.theme.ThemeViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.redcard.data.DataStoreManager
import com.example.redcard.model.MusicPlayerManager

@Composable
fun StartingPage(navController: NavController, dataStoreManager: DataStoreManager, themeViewModel: ThemeViewModel) {
    val context = LocalContext.current
    val musicEnabled by dataStoreManager.musicEnabledFlow.collectAsState(initial = true)

    LaunchedEffect(Unit) {
        if (musicEnabled) {
            MusicPlayerManager.playMusicSalon(context)
        }
    }

    val currentTheme by themeViewModel.theme.collectAsState()
    val darkTheme = when (currentTheme) {
        AppTheme.SOMBRE -> true
        AppTheme.CLAIR -> false
        AppTheme.SYSTEME -> isSystemInDarkTheme()
    }

    RedCardTheme(darkTheme = darkTheme) {
        val backgroundColor = MaterialTheme.colorScheme.background
        val textColor = MaterialTheme.colorScheme.onBackground
        val iconColor = if (darkTheme) Color.White else Color.Black

        var textPosition by remember { mutableStateOf(Offset.Zero) }
        var iconPosition by remember { mutableStateOf(Offset.Zero) }
        var firstClickDone by remember { mutableStateOf(false) }

        val infiniteTransition = rememberInfiniteTransition()
        val circleScale by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        val arrowOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Restart
            )
        )

        Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Image(
                    painter = painterResource(id = R.drawable.redcard_logo),
                    contentDescription = "Logo Red Card",
                    modifier = Modifier
                        .width(350.dp)
                        .height(350.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        navController.navigate("gameConfiguration") {
                            popUpTo("startingPage") { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text(text = "Commencer", fontSize = 20.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Première fois ?",
                    fontSize = 18.sp,
                    color = textColor,
                    modifier = Modifier
                        .clickable {
                            firstClickDone = !firstClickDone
                        }
                        .onGloballyPositioned { coordinates ->
                            textPosition = coordinates.positionInRoot()
                        }
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            // Barre de tâches en bas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (darkTheme) Color(0xFF34465B) else Color(0xFF1F2936))
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Icon(
                    imageVector = Icons.Outlined.MenuBook,
                    contentDescription = "Home",
                    tint = (if (darkTheme) iconColor else Color.White),
                    modifier = Modifier
                        .size(50.dp)
                        .clickable {
                            navController.navigate("TutorialSwipeableScreen") {
                                popUpTo("TutorialSwipeableScreen") { inclusive = true }
                            }
                        }
                        .onGloballyPositioned { coordinates ->
                            iconPosition = coordinates.positionInRoot()
                        }
                )

                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Réglages",
                    tint = (if (darkTheme) iconColor else Color.White),
                    modifier = Modifier
                        .size(50.dp)
                        .clickable {
                            navController.navigate("generalSettings") {
                                popUpTo("generalSettings") { inclusive = true }
                            }
                        }
                )
            }

            // Animation du guide visuel
            if (firstClickDone) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (iconPosition != Offset.Zero) {
                        drawCircle(
                            color = Color(0xFFFFD700),
                            radius = 30f * circleScale,
                            center = iconPosition + Offset(20f, 20f),
                            style = Stroke(width = 4f)
                        )
                    }

                    if (textPosition != Offset.Zero && iconPosition != Offset.Zero) {
                        val path = Path().apply {
                            moveTo(textPosition.x + 60f, textPosition.y + 10f)
                            quadraticBezierTo(
                                (textPosition.x + iconPosition.x) / 2,
                                (textPosition.y + iconPosition.y) / 2,
                                iconPosition.x + 20f,
                                iconPosition.y + 20f
                            )
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFFFFD700),
                            style = Stroke(
                                width = 4f,
                                pathEffect = PathEffect.dashPathEffect(
                                    intervals = floatArrayOf(30f, 30f),
                                    phase = arrowOffset * 60f
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

