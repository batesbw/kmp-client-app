package com.mass.client.feature_home.model

enum class RecentlyPlayedItemType {
    ALBUM,
    TRACK,
    // Could add PLAYLIST, ARTIST if needed later
}

data class RecentlyPlayedItemUiModel(
    val id: String, // Unique ID of the item (e.g., album.id, track.id)
    val type: RecentlyPlayedItemType,
    val title: String, // Album name or Track name
    val subtitle: String?, // e.g., Artist name(s)
    val imageUrl: String?, // URL for the cover art
    val originalItemId: String // The underlying mediaItem.itemId from the server, for actions
) 