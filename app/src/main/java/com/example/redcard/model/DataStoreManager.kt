package com.example.redcard.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Création de DataStore
val Context.dataStore by preferencesDataStore(name = "configurations")

class DataStoreManager(private val context: Context) {

    companion object {
        // Clés pour les préférences de l'utilisateur
        val PLAYERS_KEY = intPreferencesKey("players")
        val TITULAIRES_KEY = intPreferencesKey("titulaires")
        val FOOTIX_KEY = intPreferencesKey("footix")
        val REMPLACANTS_KEY = intPreferencesKey("remplacants")
        val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
        val CURRENT_LANGUAGE_KEY = stringPreferencesKey("current_language")
    }

    // Flux pour récupérer les valeurs, avec des valeurs par défaut
    val playersFlow: Flow<Int> = context.dataStore.data.map { it[PLAYERS_KEY] ?: 3 }
    val titulairesFlow: Flow<Int> = context.dataStore.data.map { it[TITULAIRES_KEY] ?: 2 }
    val footixFlow: Flow<Int> = context.dataStore.data.map { it[FOOTIX_KEY] ?: 0 }
    val remplacantsFlow: Flow<Int> = context.dataStore.data.map { it[REMPLACANTS_KEY] ?: 1 }

    val soundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[SOUND_ENABLED_KEY] ?: true }
    val currentLanguageFlow: Flow<String> = context.dataStore.data.map { it[CURRENT_LANGUAGE_KEY] ?: "Français" }

    // Méthodes pour enregistrer les valeurs
    suspend fun savePlayers(value: Int) {
        context.dataStore.edit { it[PLAYERS_KEY] = value }
    }

    suspend fun saveTitulaires(value: Int) {
        context.dataStore.edit { it[TITULAIRES_KEY] = value }
    }

    suspend fun saveFootix(value: Int) {
        context.dataStore.edit { it[FOOTIX_KEY] = value }
    }

    suspend fun saveRemplacants(value: Int) {
        context.dataStore.edit { it[REMPLACANTS_KEY] = value }
    }

    suspend fun saveSoundEnabled(value: Boolean) {
        context.dataStore.edit { it[SOUND_ENABLED_KEY] = value }
    }

    suspend fun saveCurrentLanguage(value: String) {
        context.dataStore.edit { it[CURRENT_LANGUAGE_KEY] = value }
    }
}
