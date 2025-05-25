package com.mass.client.feature_home.viewmodel

import com.mass.client.core.network.ApiService
import com.mass.client.core.model.Album
import com.mass.client.core.model.Artist
import com.mass.client.core.model.MediaType
import com.mass.client.core.model.AlbumType
import com.mass.client.core.model.ItemMapping
import com.mass.client.core.model.QueueOption
import com.mass.client.feature_home.model.UiPlayer
// UiPlayerState and UiTrack are not used at this revert point with the consolidated UiPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first // Needed for .first() on StateFlow for current value
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement // For configValueElement
import kotlinx.serialization.json.jsonPrimitive // For accessing primitive content

// This would ideally be in a common base class or injected
open class BaseViewModelProtected {
    protected val viewModelScope = CoroutineScope(Dispatchers.Main + Job())
    // TODO: Implement a proper way to cancel the Job when the ViewModel is cleared.
}

class HomeViewModel(
    private val apiService: ApiService
) : BaseViewModelProtected() {

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    val uiPlayers: StateFlow<List<UiPlayer>> = apiService.players

    private val _recentlyPlayedItems = MutableStateFlow<List<ItemMapping>>(emptyList())
    val recentlyPlayedItems: StateFlow<List<ItemMapping>> = _recentlyPlayedItems.asStateFlow()

    init {
        fetchArtistsFromApi()
        fetchAlbumsFromApi()
        fetchRecentlyPlayedItemsFromServer()
    }

    private fun fetchRecentlyPlayedItemsFromServer() {
        viewModelScope.launch {
            try {
                _recentlyPlayedItems.value = apiService.getRecentlyPlayedItems(limit = 10)
            } catch (e: Exception) {
                println("HomeViewModel: Error fetching recently played items: ${e.message}")
                _recentlyPlayedItems.value = emptyList()
            }
        }
    }

    fun onPlayerPauseClicked(playerId: String) {
        viewModelScope.launch {
            println("HomeViewModel: Pause clicked for player $playerId")
            try {
                apiService.playerCommandPlayPause(playerId)
            } catch (e: Exception) {
                println("HomeViewModel: Error calling play/pause for player $playerId: ${e.message}")
            }
        }
    }

    fun onRecentlyPlayedItemClicked(item: ItemMapping) {
        viewModelScope.launch {
            println("HomeViewModel: Card clicked on recently played: ${item.name} (ID: ${item.item_id}, Type: ${item.media_type})")
            // Placeholder for actual action
        }
    }

    fun onPlayRecentlyPlayedItem(item: ItemMapping) {
        viewModelScope.launch {
            println("HomeViewModel: Play clicked for recently played: ${item.name} (URI: ${item.uri})")
            try {
                val currentPlayerList = apiService.players.first() // Get current value of StateFlow
                if (currentPlayerList.isEmpty()) {
                    println("HomeViewModel: No players available.")
                    // Potentially try apiService.getAllPlayers() as a fallback here if critical
                    return@launch
                }

                val targetPlayer = currentPlayerList.find { it.isActivePlayer } ?: currentPlayerList.first()
                val targetPlayerId = targetPlayer.id
                println("HomeViewModel: Target player: ${targetPlayer.name} (ID: $targetPlayerId), Active: ${targetPlayer.isActivePlayer}, Playing: ${targetPlayer.isPlaying}")
                
                var selectedOption = QueueOption.PLAY // Default fallback
                if (item.media_type != null) {
                    val mediaType = item.media_type
                    try {
                        val configKey = "default_enqueue_option_${mediaType.name.lowercase()}"
                        println("HomeViewModel: Fetching config for key: $configKey")
                        val configValueElement = apiService.getCoreConfigValue("player_queues", configKey)
                        
                        val primitive = configValueElement?.jsonPrimitive
                        if (primitive != null && primitive.isString) {
                            val optionString = primitive.content
                            println("HomeViewModel: Received config value: $optionString for key: $configKey")
                            selectedOption = QueueOption.valueOf(optionString.uppercase())
                            println("HomeViewModel: Parsed QueueOption: $selectedOption")
                        } else {
                             println("HomeViewModel: Could not fetch or parse default_enqueue_option for $mediaType. Using PLAY.")
                        }
                    } catch (e: IllegalArgumentException) {
                        println("HomeViewModel: Error parsing QueueOption from config for $mediaType: ${e.message}. Using PLAY.")
                    } catch (e: Exception) {
                        println("HomeViewModel: Error fetching default_enqueue_option for $mediaType: ${e.message}. Using PLAY.")
                    }
                } else {
                    println("HomeViewModel: Item media_type is null. Using PLAY.")
                }

                if (item.uri != null) {
                    println("HomeViewModel: Requesting to play ${item.name} on player $targetPlayerId with option $selectedOption")
                    apiService.playMedia(
                        queueId = targetPlayerId,
                        media = listOf(item.uri),
                        option = selectedOption
                    )
                } else {
                    println("HomeViewModel: Item URI is null for ${item.name}, cannot play.")
                }
                
            } catch (e: Exception) {
                println("HomeViewModel: Error trying to play recently played item ${item.name}: ${e.message}")
                // No e.printStackTrace() at this revert point
            }
        }
    }

    private fun fetchArtistsFromApi() {
        viewModelScope.launch {
            try {
                _artists.value = apiService.getLibraryArtists(orderBy = "name")
                if (_artists.value.isEmpty()) { /* Placeholder data */ }
            } catch (e: Exception) {
                println("HomeViewModel: Error fetching artists: ${e.message}")
                 /* Placeholder data */
            }
        }
    }
    
    private fun fetchAlbumsFromApi() {
        viewModelScope.launch {
            try {
                _albums.value = apiService.getLibraryAlbums(orderBy = "name")
                 if (_albums.value.isEmpty()) { /* Placeholder data */ }
            } catch (e: Exception) {
                println("HomeViewModel: Error fetching albums: ${e.message}")
                /* Placeholder data */
            }
        }
    }
    // Placeholder data functions ommitted for brevity but were part of the state being reverted to
} 