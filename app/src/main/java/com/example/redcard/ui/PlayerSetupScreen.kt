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
) 
    {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var playerName by remember { mutableStateOf("") }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var photoTimestamp by remember { mutableStateOf(0L) }
    val savedPhotoUri by dataStoreManager.playerPhotoUriFlow.collectAsState(initial = null)

    // Récupérer le mot du ballon sélectionné et le rôle
    val currentBall by dataStoreManager.currentBallFlow.collectAsState(initial = null)

    // Afficher au joueur son mot et son rôle
    var showWordDialog by remember { mutableStateOf(false) }

    // Vérifier si un joueur a déjà ce nom
    // Définir d'abord le flow
    val playerNameTakenFlow = if (playerName.isBlank()) {
        // Flow qui émet toujours false
        flowOf(false)
    } else {
        dataStoreManager.isPlayerNameTakenFlow(playerName)
    }

    // Puis collecter le state de ce flow directement dans le contexte Composable
    val isPlayerNameTaken by playerNameTakenFlow.collectAsState(initial = false)
    // État pour gérer les erreurs et le chargement
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(savedPhotoUri) {
        savedPhotoUri?.let {
            currentPhotoUri = Uri.parse(it)
        }
    }
    LaunchedEffect(Unit) {
        // Réinitialiser les valeurs à chaque fois que l'écran est affiché
        playerName = ""
        currentPhotoUri = null
        errorMessage = null
        isSubmitting = false
        photoTimestamp = System.currentTimeMillis() // Force recomposition pour l'image
        
        // Réinitialiser d'abord dans le DataStore
        dataStoreManager.resetPlayerPhoto()
        dataStoreManager.resetPlayerName()
        
        // Puis réinitialiser les valeurs locales
        delay(100) // Petit délai pour s'assurer que le datastore a bien été mis à jour
        playerName = ""
        currentPhotoUri = null
        errorMessage = null
        isSubmitting = false
        photoTimestamp = System.currentTimeMillis() // Force recomposition pour l'image

    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Déclaration du lambda en tant que variable mutable pour pouvoir l'utiliser dans sa propre définition
    var takeNewPhoto: () -> Unit by remember { mutableStateOf({}) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            scope.launch {
                dataStoreManager.savePlayerPhotoUri(currentPhotoUri.toString())
                photoTimestamp = System.currentTimeMillis() // Force recomposition
            }
        } else {
            // Informer l'utilisateur que la prise de photo a échoué
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
            // Si la permission est accordée, on lance directement la caméra
            takeNewPhoto()
        } else {
            // Informer l'utilisateur que la permission est nécessaire
            Toast.makeText(
                context,
                "La permission de la caméra est nécessaire pour prendre une photo",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Définition du lambda après sa déclaration
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
                // Gérer les erreurs
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

    // Fonction pour enregistrer le joueur
    fun registerPlayer() {
        if (playerName.isBlank()) {
            errorMessage = "Le nom du joueur ne peut pas être vide"
            return
        }

        if (isPlayerNameTaken) {
            errorMessage = "Ce nom est déjà pris"
            return
        }

//        if (currentPhotoUri == null) {
//            errorMessage = "Veuillez prendre une photo"
//            return
//        }

        isSubmitting = true
        errorMessage = null
        scope.launch {
                try {
                    // Sauvegarder le nom du joueur pour la page suivante
                    dataStoreManager.savePlayerName(playerName)

                    // Enregistrer le joueur avec son rôle et son mot
                    dataStoreManager.registerPlayer(playerName, currentPhotoUri?.toString())

                    // Donner le temps au DataStore de mettre à jour ses données
                    delay(500)

                    // Forcer un rafraîchissement après navigation
                    withContext(Dispatchers.Main) {
                        navController.navigate("ChoosePlayerBallScreen") {
                            popUpTo("ChoosePlayerBallScreen") { inclusive = true }
                        }
                    }
                }catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Erreur lors de l'enregistrement: ${e.message}"
                    isSubmitting = false
                }
            }
        }
    }

    // Dialogue pour afficher le mot secret au joueur
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
                        Text("Ton mot secret est: $currentBall")
                        Text("Mémorise-le bien et ne le montre à personne !")
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
            //TODO: A enlever pour la Version finale
            // Zone pour afficher les erreurs
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

                        // Bouton pour reprendre la photo (reste comme avant)
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
                        // Ajout d'un cadre visuel pour indiquer que c'est une zone cliquable
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
                        
                        // Rendre toute la zone cliquable
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