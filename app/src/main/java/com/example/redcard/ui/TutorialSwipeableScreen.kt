package com.example.redcard.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialSwipeableScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 8 }) // Nombre de pages

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Barre supérieure avec la croix pour fermer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(onClick = { navController.navigate("startingPage") }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fermer"
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
                        .background(if (pagerState.currentPage == index) Color.Black else Color.Gray)
                )
            }
        }
    }
}

// Écrans de chaque page
@Composable
fun FirstScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Page 1", style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
fun SecondScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Page 2", style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
fun ThirdScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Page 3", style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
fun FourthScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Page 4", style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
fun FifthScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Page 5", style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
fun SixthScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Page 6", style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
fun SeventhScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Page 7", style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
fun EighthScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Page 8", style = MaterialTheme.typography.headlineLarge)
    }
}
