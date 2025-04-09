package com.example.redcard.model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.redcard.data.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TurnManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // Clés pour stocker les informations de tour dans DataStore
    companion object {
        val CURRENT_TURN_KEY = intPreferencesKey("current_turn")
        val CURRENT_PLAYER_INDEX_KEY = intPreferencesKey("current_player_index")
        val PLAYERS_ORDER_KEY = stringPreferencesKey("players_order")
    }

    // Flow pour observer le tour actuel
    val currentTurnFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[CURRENT_TURN_KEY] ?: 1
    }

    // Flow pour observer l'index du joueur actuel
    val currentPlayerIndexFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[CURRENT_PLAYER_INDEX_KEY] ?: 0
    }

    // Flow pour observer l'ordre des joueurs
    val playersOrderFlow: Flow<List<Player>> = dataStore.data.map { preferences ->
        val playersOrderJson = preferences[PLAYERS_ORDER_KEY]
        if (playersOrderJson != null) {
            try {
                Json.decodeFromString(ListSerializer(Player.serializer()), playersOrderJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Définir l'ordre des joueurs pour toute la partie
    suspend fun setPlayersOrder(players: List<Player>) {
        dataStore.edit { preferences ->
            val playersOrderJson = Json.encodeToString(
                ListSerializer(Player.serializer()),
                players
            )
            preferences[PLAYERS_ORDER_KEY] = playersOrderJson
            // Réinitialiser l'index du joueur actuel à 0
            preferences[CURRENT_PLAYER_INDEX_KEY] = 0
            // Commencer au tour 1
            preferences[CURRENT_TURN_KEY] = 1
        }
    }

    // Générer un ordre de passage initial pour la partie
    suspend fun generateInitialPlayersOrder(registeredPlayers: List<Player>): List<Player> {
        // Filtrer les joueurs qui ne sont pas Footix pour le premier joueur
        val nonFootixPlayers = registeredPlayers.filter { it.role != "Footix" }

        if (nonFootixPlayers.isEmpty() || registeredPlayers.isEmpty()) {
            return emptyList()
        }

        // Choisir aléatoirement un joueur non-Footix comme premier joueur
        val firstPlayer = nonFootixPlayers.random()

        // Prendre les autres joueurs et les mélanger
        val restOfPlayers = registeredPlayers.filter { it.id != firstPlayer.id }.shuffled()

        // Créer l'ordre final: le premier joueur suivi des autres joueurs mélangés
        val finalOrder = listOf(firstPlayer) + restOfPlayers

        // Enregistrer cet ordre dans DataStore
        setPlayersOrder(finalOrder)

        return finalOrder
    }

    // Passer au joueur suivant
    suspend fun moveToNextPlayer() {
        dataStore.edit { preferences ->
            val currentIndex = preferences[CURRENT_PLAYER_INDEX_KEY] ?: 0
            preferences[CURRENT_PLAYER_INDEX_KEY] = currentIndex + 1
        }
    }

    // Passer au tour suivant et réinitialiser l'index du joueur
    suspend fun moveToNextTurn() {
        dataStore.edit { preferences ->
            val currentTurn = preferences[CURRENT_TURN_KEY] ?: 1
            preferences[CURRENT_TURN_KEY] = currentTurn + 1
            preferences[CURRENT_PLAYER_INDEX_KEY] = 0
        }
    }

    // Réinitialiser le gestionnaire de tours
    suspend fun resetTurnManager() {
        dataStore.edit { preferences ->
            preferences.remove(CURRENT_TURN_KEY)
            preferences.remove(CURRENT_PLAYER_INDEX_KEY)
            preferences.remove(PLAYERS_ORDER_KEY)
        }
    }
    
    // Nouvelle méthode pour synchroniser avec les joueurs actuels du DataStore
    suspend fun syncPlayersWithDataStore(registeredPlayersFlow: Flow<List<Player>>) {
        val currentPlayers = registeredPlayersFlow.first()
        val currentOrder = playersOrderFlow.first()
        
        // Si l'ordre est vide ou si les joueurs ont changé, générer un nouvel ordre
        if (currentOrder.isEmpty() || !comparePlayerLists(currentOrder, currentPlayers)) {
            generateInitialPlayersOrder(currentPlayers)
        }
    }
    
    // Méthode auxiliaire pour comparer deux listes de joueurs
    private fun comparePlayerLists(list1: List<Player>, list2: List<Player>): Boolean {
        if (list1.size != list2.size) return false
        
        // Créer des maps d'ID vers joueurs pour les deux listes
        val map1 = list1.associateBy { it.id }
        val map2 = list2.associateBy { it.id }
        
        // Vérifier si tous les joueurs de la première liste sont dans la seconde
        return map1.keys.all { id -> map2.containsKey(id) }
    }
}