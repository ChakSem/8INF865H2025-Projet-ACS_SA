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
    var photoTimestamp by remember { mutableStateOf(0L) }
    val savedPhotoUri by dataStoreManager.playerPhotoUriFlow.collectAsState(initial = null)

    LaunchedEffect(savedPhotoUri) {
        savedPhotoUri?.let {
            currentPhotoUri = Uri.parse(it)
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            scope.launch {
                dataStoreManager.savePlayerPhotoUri(currentPhotoUri.toString())
                photoTimestamp = System.currentTimeMillis() // Force recomposition
            }
        }
    }

    fun takeNewPhoto() {
        if (hasCameraPermission) {
            val photoFile = dataStoreManager.getPhotoFile()
            currentPhotoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )

            cameraLauncher.launch(currentPhotoUri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
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
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                key(photoTimestamp) { //Pour forcer la recomposition lorsque on se reprend en photo
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
                        IconButton(
                            onClick = { takeNewPhoto() }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = "Prendre une photo",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Nom du joueur") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    scope.launch {
                        dataStoreManager.savePlayerName(playerName)
                        navController.navigate("PlayerWordDiscoverScreen")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = playerName.isNotBlank() && currentPhotoUri != null
            ) {
                Text("Lire mon mot ")
            }
        }
    }
}
