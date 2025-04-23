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

/**
 * Gère le système de tours pendant une partie, en maintenant l'ordre des joueurs
 * et le joueur actuel à travers les sessions grâce au DataStore.
 */
class TurnManager (
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val CURRENT_TURN_KEY = intPreferencesKey("current_turn")
        val CURRENT_PLAYER_INDEX_KEY = intPreferencesKey("current_player_index")
        val PLAYERS_ORDER_KEY = stringPreferencesKey("players_order")
    }

    /**
     * Flow pour observer l'ordre des joueurs durant la partie.
     */
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

    /**
     * Définit l'ordre des joueurs pour toute la partie et réinitialise les compteurs.
     */
    suspend fun setPlayersOrder(players: List<Player>) {
        dataStore.edit { preferences ->
            val playersOrderJson = Json.encodeToString(
                ListSerializer(Player.serializer()),
                players
            )
            preferences[PLAYERS_ORDER_KEY] = playersOrderJson
            preferences[CURRENT_PLAYER_INDEX_KEY] = 0
            preferences[CURRENT_TURN_KEY] = 1
        }
    }

    /**
     * Génère un ordre de passage initial où un joueur non-Footix commence toujours.
     * Les autres joueurs sont placés aléatoirement.
     */
    suspend fun generateInitialPlayersOrder(registeredPlayers: List<Player>): List<Player> {
        val nonFootixPlayers = registeredPlayers.filter { it.role != "Footix" }

        if (nonFootixPlayers.isEmpty() || registeredPlayers.isEmpty()) {
            return emptyList()
        }

        val firstPlayer = nonFootixPlayers.random()
        val restOfPlayers = registeredPlayers.filter { it.id != firstPlayer.id }.shuffled()
        val finalOrder = listOf(firstPlayer) + restOfPlayers

        setPlayersOrder(finalOrder)
        return finalOrder
    }

    /**
     * Réinitialise toutes les données du gestionnaire de tours.
     */
    suspend fun resetTurnManager() {
        dataStore.edit { preferences ->
            preferences.remove(CURRENT_TURN_KEY)
            preferences.remove(CURRENT_PLAYER_INDEX_KEY)
            preferences.remove(PLAYERS_ORDER_KEY)
        }
    }

    /**
     * Synchronise l'ordre des joueurs avec la liste actuelle des joueurs enregistrés.
     * Génère un nouvel ordre si nécessaire.
     */
    suspend fun syncPlayersWithDataStore(registeredPlayersFlow: Flow<List<Player>>) {
        val currentPlayers = registeredPlayersFlow.first()
        val currentOrder = playersOrderFlow.first()

        if (currentOrder.isEmpty() || !comparePlayerLists(currentOrder, currentPlayers)) {
            generateInitialPlayersOrder(currentPlayers)
        }
    }

    /**
     * Compare deux listes de joueurs en fonction de leurs IDs.
     */
    private fun comparePlayerLists(list1: List<Player>, list2: List<Player>): Boolean {
        if (list1.size != list2.size) return false

        val map1 = list1.associateBy { it.id }
        val map2 = list2.associateBy { it.id }

        return map1.keys.all { id -> map2.containsKey(id) }
    }
}