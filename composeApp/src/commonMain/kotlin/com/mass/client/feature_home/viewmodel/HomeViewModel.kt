package com.mass.client.feature_home.viewmodel

import com.mass.client.core.network.ApiService
import com.mass.client.core.model.Album
import com.mass.client.core.model.Artist
import com.mass.client.core.model.MediaType
import com.mass.client.core.model.AlbumType
import com.mass.client.core.model.ItemMapping
import com.mass.client.feature_home.model.UiPlayer
import com.mass.client.feature_home.model.UiPlayerState
import com.mass.client.feature_home.model.UiTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// This would ideally be in a common base class or injected
open class BaseViewModelProtected {
    protected val viewModelScope = CoroutineScope(Dispatchers.Main + Job())
    // TODO: Implement a proper way to cancel the Job when the ViewModel is cleared.
}

class HomeViewModel(
    private val apiService: ApiService // ApiService now provides the players flow
) : BaseViewModelProtected() {

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    // New StateFlow for UI Players, now directly from ApiService
    val uiPlayers: StateFlow<List<UiPlayer>> = apiService.players

    // StateFlow for Recently Played items, now using ItemMapping
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
                // Directly assign the List<ItemMapping> from the API service
                _recentlyPlayedItems.value = apiService.getRecentlyPlayedItems(limit = 10)
            } catch (e: Exception) {
                println("HomeViewModel: Error fetching recently played items: ${e.message}")
                _recentlyPlayedItems.value = emptyList() // Clear on error
            }
        }
    }

    fun onPlayerPauseClicked(playerId: String) {
        viewModelScope.launch {
            println("HomeViewModel: Pause clicked for player $playerId - Calling ApiService")
            try {
                apiService.playerCommandPlayPause(playerId)
                // The UI should update automatically due to collecting apiService.players
            } catch (e: Exception) {
                println("HomeViewModel: Error calling play/pause for player $playerId: ${e.message}")
                // Handle error appropriately (e.g., show a snackbar)
            }
        }
    }

    fun onRecentlyPlayedItemClicked(item: ItemMapping) { // Changed parameter to ItemMapping
        viewModelScope.launch {
            // Using item.name which is part of ItemMapping
            println("HomeViewModel: Clicked on recently played: ${item.name} (ID: ${item.item_id}, Type: ${item.media_type})")
            // TODO: Implement navigation or play action via ApiService
            // e.g., apiService.playMedia(queueId = /* target queue */, media = listOf(item.uri), option = QueueOption.PLAY)
        }
    }

    private fun fetchArtistsFromApi() {
        viewModelScope.launch {
            try {
                _artists.value = apiService.getLibraryArtists(orderBy = "name")
                // Placeholder data if API fails or returns empty for testing
                if (_artists.value.isEmpty()) {
                    _artists.value = listOf(
                        Artist(
                            item_id = "1", name = "Artist One", provider = "placeholder", uri = "uri:artist:1",
                            media_type = MediaType.artist, is_playable = false
                        ),
                        Artist(
                            item_id = "2", name = "Artist Two", provider = "placeholder", uri = "uri:artist:2",
                            media_type = MediaType.artist, is_playable = false
                        )
                    ).sortedBy { it.name }
                }
            } catch (e: Exception) {
                println("HomeViewModel: Error fetching artists: ${e.message}")
                // Fallback to placeholder data on error for testing
                _artists.value = listOf(
                    Artist(
                        item_id = "placeholder_artist_1", name = "Fallback Artist A", provider = "fallback", uri = "uri:fallback_artist:1",
                        media_type = MediaType.artist, is_playable = false
                    ),
                    Artist(
                        item_id = "placeholder_artist_2", name = "Fallback Artist B", provider = "fallback", uri = "uri:fallback_artist:2",
                        media_type = MediaType.artist, is_playable = false
                    )
                ).sortedBy { it.name }
            }
        }
    }
    
    private fun fetchAlbumsFromApi() {
        viewModelScope.launch {
            try {
                _albums.value = apiService.getLibraryAlbums(orderBy = "name")
                // Placeholder data if API fails or returns empty for testing
                if (_albums.value.isEmpty()) {
                    val artistOne = Artist(
                        item_id = "1", name = "Artist One", provider = "placeholder", uri = "uri:artist:1",
                        media_type = MediaType.artist, is_playable = false
                    )
                    _albums.value = listOf(
                        Album(
                            item_id = "a1", name = "Album Alpha", provider = "placeholder", uri = "uri:album:a1",
                            media_type = MediaType.album, is_playable = false, artists = listOf(artistOne),
                            album_type = AlbumType.album
                        ),
                        Album(
                            item_id = "a2", name = "Beta Collection", provider = "placeholder", uri = "uri:album:a2",
                            media_type = MediaType.album, is_playable = false, artists = listOf(artistOne), // Assuming same artist for simplicity
                            album_type = AlbumType.compilation
                        )
                    ).sortedBy { it.name }
                }
            } catch (e: Exception) {
                println("HomeViewModel: Error fetching albums: ${e.message}")
                // Fallback to placeholder data on error for testing
                val fallbackArtist = Artist(
                    item_id = "placeholder_artist_1", name = "Fallback Artist A", provider = "fallback", uri = "uri:fallback_artist:1",
                    media_type = MediaType.artist, is_playable = false
                )
                _albums.value = listOf(
                    Album(
                        item_id = "fallback_album_1", name = "Fallback Album X", provider = "fallback", uri = "uri:fallback_album:1",
                        media_type = MediaType.album, is_playable = false, artists = listOf(fallbackArtist),
                        album_type = AlbumType.album
                    ),
                    Album(
                        item_id = "fallback_album_2", name = "Fallback Album Y", provider = "fallback", uri = "uri:fallback_album:2",
                        media_type = MediaType.album, is_playable = false, artists = listOf(fallbackArtist),
                        album_type = AlbumType.album
                    )
                ).sortedBy { it.name }
            }
        }
    }
} 