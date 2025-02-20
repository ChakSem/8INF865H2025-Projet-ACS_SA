package com.example.redcard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.redcard.R

@Composable
fun StartingPage(navController: NavController) {
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
            modifier = Modifier.clickable {
                navController.navigate("tutorial") {
                    popUpTo("startingPage") { inclusive = true }
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Barre de navigation en bas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Icône Tutoriel (redirige vers HomeScreen)
            Icon(
                painter = painterResource(id = R.drawable.ic_book),
                contentDescription = "Home",
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        //Mettre la redirection vers le tutoriel apres
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
            )

            // Icône Paramètres (redirige vers la page des paramètres)
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
}
