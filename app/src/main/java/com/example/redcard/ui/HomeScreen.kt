package com.example.redcard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.redcard.R

@Composable
fun HomeScreen(
    navController: NavController, // Ajout du NavController
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    innerPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // Icône maison et titre
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_flag_fr),
                contentDescription = "Accueil",
                modifier = Modifier.size(24.dp)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "Parametrages",
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        navController.navigate("generalSettings") {
                            popUpTo("generalSettings") { inclusive = true }
                        }
                    }
            )
        }
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Logo Red Card",
            modifier = Modifier
                .width(250.dp)
                .height(250.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { navController.navigate("startingPage") }) {
            Text(text = "Appuyer pour continuer")
        }
        
    }
}

