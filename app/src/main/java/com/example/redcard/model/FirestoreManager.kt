package com.example.redcard.model

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.File

class FirestoreManager(private val context: Context) {

    companion object {
        private const val COLLECTION_NAME = "configurations"

        // Clés pour les préférences de l'utilisateur (nom des champs dans Firestore)
        const val PLAYERS_KEY = "players"
        const val TITULAIRES_KEY = "titulaires"
        const val FOOTIX_KEY = "footix"
        const val REMPLACANTS_KEY = "remplacants"
        const val SOUND_ENABLED_KEY = "sound_enabled"
        const val CURRENT_LANGUAGE_KEY = "current_language"
        const val SECRET_WORDS_KEY = "secret_words"
        const val SELECTED_BALL_WORD_KEY = "selected_ball_word"
        const val PLAYER_PHOTO_URI_KEY = "player_photo_uri"
        const val PLAYER_NAME_KEY = "player_name"
        const val SELECTED_BALLS_KEY = "selected_balls"
    }

    private val db = FirebaseFirestore.getInstance()

    // Flux pour récupérer les mots secrets
    private val defaultSecretWords = setOf(
        "Zidane", "Platini", "Benzema", "Mbappé", "Henry",
        "Cantona", "Papin", "Griezmann", "Thuram", "Deschamps"
    )

    val secretWordsFlow: Flow<Set<String>> = flow {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        val snapshot = docRef.get().await()
        emit(snapshot.get(SECRET_WORDS_KEY) as? Set<String> ?: defaultSecretWords)
    }

    val selectedBallWordFlow: Flow<String?> = flow {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        val snapshot = docRef.get().await()
        emit(snapshot.get(SELECTED_BALL_WORD_KEY) as? String)
    }

    suspend fun saveSecretWords(words: Set<String>) {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        docRef.update(SECRET_WORDS_KEY, words).await()
    }

    suspend fun saveSelectedBallWord(word: String) {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        docRef.update(SELECTED_BALL_WORD_KEY, word).await()
    }

    // Flux pour récupérer les valeurs avec des valeurs par défaut
    val playersFlow: Flow<Int> = flow {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        val snapshot = docRef.get().await()
        emit(snapshot.get(PLAYERS_KEY) as? Int ?: 3)
    }

    val titulairesFlow: Flow<Int> = flow {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        val snapshot = docRef.get().await()
        emit(snapshot.get(TITULAIRES_KEY) as? Int ?: 2)
    }

    val footixFlow: Flow<Int> = flow {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        val snapshot = docRef.get().await()
        emit(snapshot.get(FOOTIX_KEY) as? Int ?: 0)
    }

    val remplacantsFlow: Flow<Int> = flow {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        val snapshot = docRef.get().await()
        emit(snapshot.get(REMPLACANTS_KEY) as? Int ?: 1)
    }

    val rolesFlow: Flow<List<String>> = combine(
        titulairesFlow,
        footixFlow,
        remplacantsFlow
    ) { titulaires, footix, remplacants ->
        List(titulaires) { "Titulaire" } +
                List(footix) { "Footix" } +
                List(remplacants) { "Remplaçant" }
    }.map { it.shuffled() } // Mélanger les rôles pour éviter un ordre prévisible

    val soundEnabledFlow: Flow<Boolean> = flow {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        val snapshot = docRef.get().await()
        emit(snapshot.get(SOUND_ENABLED_KEY) as? Boolean ?: true)
    }

    val currentLanguageFlow: Flow<String> = flow {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        val snapshot = docRef.get().await()
        emit(snapshot.get(CURRENT_LANGUAGE_KEY) as? String ?: "Français")
    }

    // Méthodes pour enregistrer les valeurs dans Firestore
    suspend fun savePlayers(value: Int) {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        docRef.update(PLAYERS_KEY, value).await()
    }

    suspend fun saveTitulaires(value: Int) {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        docRef.update(TITULAIRES_KEY, value).await()
    }

    suspend fun saveFootix(value: Int) {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        docRef.update(FOOTIX_KEY, value).await()
    }

    suspend fun saveRemplacants(value: Int) {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        docRef.update(REMPLACANTS_KEY, value).await()
    }

    suspend fun saveSoundEnabled(value: Boolean) {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        docRef.update(SOUND_ENABLED_KEY, value).await()
    }

    suspend fun saveCurrentLanguage(value: String) {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        docRef.update(CURRENT_LANGUAGE_KEY, value).await()
    }

    // Flux des ballons sélectionnés
    val selectedBallsFlow: Flow<Set<String>> = flow {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        val snapshot = docRef.get().await()
        emit(snapshot.get(SELECTED_BALLS_KEY) as? Set<String> ?: emptySet())
    }

    // Sauvegarder un ballon sélectionné
    suspend fun saveSelectedBall(word: String) {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        val updatedBalls = mutableSetOf<String>().apply {
            val currentBalls = docRef.get().await().get(SELECTED_BALLS_KEY) as? Set<String> ?: emptySet()
            addAll(currentBalls)
            add(word)
        }
        docRef.update(SELECTED_BALLS_KEY, updatedBalls).await()
    }

    // Nouveau flux pour l'URI de la photo
    val playerPhotoUriFlow: Flow<String?> = flow {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        val snapshot = docRef.get().await()
        emit(snapshot.get(PLAYER_PHOTO_URI_KEY) as? String)
    }

    val playerNameFlow: Flow<String?> = flow {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        val snapshot = docRef.get().await()
        emit(snapshot.get(PLAYER_NAME_KEY) as? String)
    }

    suspend fun savePlayerPhotoUri(uri: String) {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        docRef.update(PLAYER_PHOTO_URI_KEY, uri).await()
    }

    suspend fun savePlayerName(name: String) {
        val docRef = db.collection(COLLECTION_NAME).document("preferences")
        docRef.update(PLAYER_NAME_KEY, name).await()
    }

    // Obtenir un fichier photo (enregistré localement, pas directement depuis Firestore)
    fun getPhotoFile(): File {
        val photoDir = File(context.filesDir, "player_photos")
        if (!photoDir.exists()) {
            photoDir.mkdirs()
        }
        return File(photoDir, "player_photo_${System.currentTimeMillis()}.jpg")
    }
}
