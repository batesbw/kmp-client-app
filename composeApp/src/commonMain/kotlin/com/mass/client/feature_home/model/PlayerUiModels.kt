package com.mass.client.feature_home.model

// UI-specific models for the Players section

data class UiPlayer(
    val id: String,
    val name: String,
    val state: UiPlayerState,
    val currentTrack: UiTrack? = null,
    val albumArtUri: String? = null // Can be derived from track or player specific
)

data class UiTrack(
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUri: String? = null // Often part of track metadata
)

enum class UiPlayerState {
    PLAYING,
    PAUSED,
    IDLE,
    OFF
} 