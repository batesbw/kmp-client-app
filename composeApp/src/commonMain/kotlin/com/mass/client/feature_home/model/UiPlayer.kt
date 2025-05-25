package com.mass.client.feature_home.model

import io.music_assistant.client.data.model.server.PlayerState // Corrected import

// This is the UiPlayer state we were aiming for just before the build fix cycles.
// It includes fields that PlayerCard.kt was adapted to use.
data class UiPlayer(
    val id: String,
    val name: String,
    val isActivePlayer: Boolean, // True if this is the currently selected/active player for the app UI
    val isPlaying: Boolean,      // True if content is currently physically playing on this player
    val artworkUrl: String? = null, // Optional: for displaying current track art on player
    val currentTrackName: String? = null // Optional: for displaying current track
)

// Mapper from io.music_assistant.client.data.model.client.PlayerData to UiPlayer
fun io.music_assistant.client.data.model.client.PlayerData.toUiPlayer(activePlayerIdFromStore: String?): UiPlayer {
    val isActiveForApp = this.player.id == activePlayerIdFromStore
    
    // isPlaying comes directly from the source PlayerData's player object
    // artworkUrl and currentTrackName come from the queue's current item's track details
    return UiPlayer(
        id = this.player.id,
        name = this.player.name,
        isActivePlayer = isActiveForApp,
        isPlaying = this.player.isPlaying, 
        artworkUrl = this.queue?.currentItem?.track?.imageUrl, 
        currentTrackName = this.queue?.currentItem?.track?.name 
    )
} 