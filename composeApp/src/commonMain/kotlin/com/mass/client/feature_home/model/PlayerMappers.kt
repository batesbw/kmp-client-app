package com.mass.client.feature_home.model

// Source models from the old data layer
import io.music_assistant.client.data.model.client.PlayerData as OldPlayerData
import io.music_assistant.client.data.model.server.ServerMediaItem // Import ServerMediaItem
import io.music_assistant.client.data.model.server.MediaType as ServerMediaType // Import ServerMediaType
// AppMediaItem is implicitly used via OldPlayerData.queue.currentItem.track
// import io.music_assistant.client.data.model.client.AppMediaItem

// Target UI models (UiPlayer, UiTrack, UiPlayerState) are in this package

fun OldPlayerData.toUiPlayer(): UiPlayer {
    val oldPlayer = this.player // This is io.music_assistant.client.data.model.client.Player
    val queue = this.queue
    val currentQueueTrack = queue?.currentItem?.track // This is AppMediaItem.Track

    // State mapping based on client.Player.isPlaying and presence of a current track.
    // This is a simplification because client.Player does not expose the rich server.PlayerState.
    // Ideally, PlayerData would include the original server.PlayerState to differentiate PAUSED, IDLE, OFF accurately.
    val derivedUiState = when {
        oldPlayer.isPlaying -> UiPlayerState.PLAYING
        currentQueueTrack != null -> UiPlayerState.PAUSED // If not playing but has a track, assume PAUSED
        else -> UiPlayerState.IDLE // If not playing and no track, assume IDLE. Cannot determine OFF.
    }

    return UiPlayer(
        id = oldPlayer.id,
        name = oldPlayer.name,
        state = derivedUiState, // This state is derived and limited by the source data.
        currentTrack = currentQueueTrack?.let {
            UiTrack(
                title = it.name, // AppMediaItem.Track.name
                artist = it.artists?.joinToString { artist -> artist.name } ?: "Unknown Artist", // AppMediaItem.Track.artists
                album = "Unknown Album", // AppMediaItem.Track doesn't directly link to an AppMediaItem.Album name.
                                         // it.imageUrl might be album art or track-specific art.
                albumArtUri = it.imageUrl // AppMediaItem.imageUrl (from metadata.images)
            )
        },
        // Use the track's image as the primary album art for the player card if available.
        // oldPlayer (client.Player) does not have an image field.
        albumArtUri = currentQueueTrack?.imageUrl 
    )
}

// This specific OldPlayerState.toUiPlayerState() might not be usable if OldPlayerState is not directly available.
// The UiPlayerState should be derived inside toUiPlayer() based on available data.
/* // Removing this as it might be misleading given current data access
fun OldServerPlayerState.toUiPlayerState(): UiPlayerState {
    return when (this) {
        OldServerPlayerState.PLAYING -> UiPlayerState.PLAYING
        OldServerPlayerState.PAUSED -> UiPlayerState.PAUSED
        OldServerPlayerState.IDLE -> UiPlayerState.IDLE
        OldServerPlayerState.OFF -> UiPlayerState.OFF
        // else -> UiPlayerState.OFF // Or handle as an error / unknown state
    }
}
*/

// The OldTrack.toUiTrack() is also not directly applicable as we get AppMediaItem.Track
/* // Removing this as it's not directly used with AppMediaItem.Track
fun OldTrack.toUiTrack(): UiTrack { // OldTrack is io.music_assistant.client.data.model.client.Track
    return UiTrack(
        title = this.name,
        artist = this.artists.joinToString { it.name },
        album = this.album?.name ?: "Unknown Album",
        albumArtUri = this.image
    )
}
*/

fun ServerMediaItem.toRecentlyPlayedItemUiModel(): RecentlyPlayedItemUiModel {
    val itemType = when (this.mediaType) {
        ServerMediaType.ALBUM -> RecentlyPlayedItemType.ALBUM
        ServerMediaType.TRACK -> RecentlyPlayedItemType.TRACK
        // Add other types if the server can return them in recently played history
        else -> RecentlyPlayedItemType.TRACK // Default or handle as unknown
    }
    
    val subtitle = this.artists?.takeIf { it.isNotEmpty() }?.joinToString { it.name } // Use only artists list

    return RecentlyPlayedItemUiModel(
        id = "recently_played_${this.provider}_${this.itemId}", // Create a unique UI-level ID
        type = itemType,
        title = this.name,
        subtitle = subtitle,
        imageUrl = this.metadata?.images?.firstOrNull()?.path, // Use first image from metadata
        originalItemId = this.itemId // The actual ID to use for actions
    )
} 