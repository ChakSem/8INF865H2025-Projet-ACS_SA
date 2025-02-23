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

import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
@Composable
fun StartingPage(navController: NavController) {
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = {
                    navController.navigate("gameConfiguration") {
                        popUpTo("startingPage") { inclusive = true }
                    }
                },
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(text = "Commencer", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Première fois ?",
                fontSize = 18.sp,
                modifier = Modifier
                    .clickable {
                        firstClickDone = !firstClickDone
                    }
                    .onGloballyPositioned { coordinates ->
                        textPosition = coordinates.positionInRoot()
                    }
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_book),
                    contentDescription = "Home",
                    modifier = Modifier
                        .size(40.dp)
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
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Paramètres",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            navController.navigate("generalSettings") {
                                popUpTo("generalSettings") { inclusive = true }
                            }
                        }
                )
            }
        }

        if (firstClickDone) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (iconPosition != Offset.Zero) {
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 30f * circleScale,  // Ajusté pour la taille de l'icône (TODO : a adapter)
                        center = iconPosition + Offset(20f, 20f),  // Centré sur l'icône (TODO : a adapter)
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