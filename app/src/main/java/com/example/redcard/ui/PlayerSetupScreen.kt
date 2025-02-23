package com.example.redcard.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.redcard.R
import com.example.redcard.data.DataStoreManager
import kotlinx.coroutines.launch

@Composable
fun PlayerSetupScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var playerName by remember { mutableStateOf("") }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val selectedWord by dataStoreManager.selectedBallWordFlow.collectAsState(initial = null)

    // Launcher pour la caméra - déclaré AVANT son utilisation dans permissionLauncher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            scope.launch {
                dataStoreManager.savePlayerPhotoUri(currentPhotoUri.toString())
            }
        }
    }

    // Gérer la permission de la caméra
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher pour demander la permission
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            val newPhotoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                dataStoreManager.getPhotoFile()
            )
            currentPhotoUri = newPhotoUri
            cameraLauncher.launch(newPhotoUri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choisis un nom") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Zone de photo
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (currentPhotoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(currentPhotoUri),
                        contentDescription = "Photo de profil",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    IconButton(
                        onClick = {
                            if (hasCameraPermission) {
                                val newPhotoUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    dataStoreManager.getPhotoFile()
                                )
                                currentPhotoUri = newPhotoUri
                                cameraLauncher.launch(newPhotoUri)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera),
                            contentDescription = "Prendre une photo",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Nom du joueur") },
                modifier = Modifier.fillMaxWidth()
            )

            selectedWord?.let {
                Text(
                    text = "Ton mot secret est: $it",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        dataStoreManager.savePlayerName(playerName)
                        navController.navigate("game_screen")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = playerName.isNotBlank() && currentPhotoUri != null
            ) {
                Text("Continuer")
            }
        }
    }
}