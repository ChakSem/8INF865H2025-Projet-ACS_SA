package com.example.redcard.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.redcard.R
import com.example.redcard.data.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.flowOf

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
    var photoTimestamp by remember { mutableStateOf(0L) }
    val savedPhotoUri by dataStoreManager.playerPhotoUriFlow.collectAsState(initial = null)
    val currentBall by dataStoreManager.currentBallFlow.collectAsState(initial = null)
    var showWordDialog by remember { mutableStateOf(false) }

    // Vérifier si un joueur a déjà ce nom
    val playerNameTakenFlow = if (playerName.isBlank()) {
        flowOf(false)
    } else {
        dataStoreManager.isPlayerNameTakenFlow(playerName)
    }

    val isPlayerNameTaken by playerNameTakenFlow.collectAsState(initial = false)
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(savedPhotoUri) {
        savedPhotoUri?.let {
            currentPhotoUri = Uri.parse(it)
        }
    }

    LaunchedEffect(Unit) {
        // Réinitialiser les valeurs à chaque fois que l'écran est affiché
        dataStoreManager.resetPlayerPhoto()
        dataStoreManager.resetPlayerName()

        delay(100) // Petit délai pour s'assurer que le datastore a bien été mis à jour
        playerName = ""
        currentPhotoUri = null
        errorMessage = null
        isSubmitting = false
        photoTimestamp = System.currentTimeMillis()
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var takeNewPhoto: () -> Unit by remember { mutableStateOf({}) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            scope.launch {
                dataStoreManager.savePlayerPhotoUri(currentPhotoUri.toString())
                photoTimestamp = System.currentTimeMillis()
            }
        } else {
            Toast.makeText(
                context,
                "La prise de photo a échoué, veuillez réessayer",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            takeNewPhoto()
        } else {
            Toast.makeText(
                context,
                "La permission de la caméra est nécessaire pour prendre une photo",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    takeNewPhoto = {
        if (hasCameraPermission) {
            val photoFile = dataStoreManager.getPhotoFile()
            try {
                currentPhotoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                cameraLauncher.launch(currentPhotoUri)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Erreur lors de la préparation de l'appareil photo: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun registerPlayer() {
        if (playerName.isBlank()) {
            errorMessage = "Le nom du joueur ne peut pas être vide"
            return
        }

        if (isPlayerNameTaken) {
            errorMessage = "Ce nom est déjà pris"
            return
        }

        isSubmitting = true
        errorMessage = null
        scope.launch {
            try {
                dataStoreManager.savePlayerName(playerName)
                dataStoreManager.registerPlayer(playerName, currentPhotoUri?.toString())

                delay(500)

                withContext(Dispatchers.Main) {
                    navController.navigate("ChoosePlayerBallScreen") {
                        popUpTo("ChoosePlayerBallScreen") { inclusive = true }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Erreur lors de l'enregistrement: ${e.message}"
                    isSubmitting = false
                }
            }
        }
    }

    if (showWordDialog) {
        AlertDialog(
            onDismissRequest = { showWordDialog = false },
            title = { Text("Ton rôle dans le jeu") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (currentBall != null) {
                        Text("Ton mot secret est:", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "$currentBall",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )

                        Text("\nMémorise-le bien et ne le montre à personne !")
                    } else {
                        Text("Tu est le Footix !")
                        Text("Tu dois deviner le mot secret des autres joueurs.")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { registerPlayer() },
                    enabled = !isSubmitting && playerName.isNotBlank() && !isPlayerNameTaken
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("J'ai mémorisé")
                    }
                }
            },
            dismissButton = {
                Button(onClick = { showWordDialog = false }) {
                    Text("Annuler")
                }
            }
        )
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
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                key(photoTimestamp) {
                    if (currentPhotoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = currentPhotoUri,
                                error = painterResource(id = R.drawable.ic_camera)
                            ),
                            contentDescription = "Photo de profil",
                            modifier = Modifier.fillMaxSize()
                        )

                        IconButton(
                            onClick = { takeNewPhoto() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = "Reprendre une photo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = "Prendre une photo",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Prendre une photo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { takeNewPhoto() }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Nom du joueur") },
                modifier = Modifier.fillMaxWidth(),
                isError = isPlayerNameTaken,
                supportingText = {
                    if (isPlayerNameTaken) {
                        Text("Ce nom est déjà pris", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Button(
                onClick = { showWordDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = playerName.isNotBlank() && !isPlayerNameTaken
            ) {
                Text("Voir mon mot secret")
            }
        }
    }
}