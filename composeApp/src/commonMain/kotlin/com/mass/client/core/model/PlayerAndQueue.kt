package com.mass.client.core.model

import kotlinx.serialization.Serializable

// Based on frontend/src/plugins/api/interfaces.ts

@Serializable
data class DeviceInfo(
    val model: String? = null, // Made nullable as it might not always be present
    val manufacturer: String? = null, // Made nullable
    // Skipping other fields like software_version, ip_address for now
)

@Serializable
data class PlayerMedia(
    val uri: String,
    val media_type: MediaType, // Enum
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val image_url: String? = null,
    val duration: Float? = null, // Changed from Int to Float for decimal durations
    val queue_id: String? = null,
    val queue_item_id: String? = null
)

@Serializable
data class PlayerSource(
    val id: String,
    val name: String
    // Skipping passive, can_play_pause etc. for initial simplicity
)

@Serializable
data class Player(
    val player_id: String,
    val provider: String,
    val type: PlayerType, // Enum
    val name: String,
    val available: Boolean,
    val device_info: DeviceInfo? = null,
    val supported_features: List<PlayerFeature> = emptyList(), // Enum list
    val enabled: Boolean,

    val state: PlayerState? = null, // Enum
    val powered: Boolean? = null,
    val volume_level: Int? = null,
    val volume_muted: Boolean? = null,
    val elapsed_time: Float? = null, // Using Float for more precision if server sends it
    val elapsed_time_last_updated: Double? = null, // Changed from Long to Double for decimal timestamps
    val current_media: PlayerMedia? = null,

    val group_childs: List<String> = emptyList(),
    val active_source: String? = null,
    // val source_list: List<PlayerSource> = emptyList(), // Can add later if source switching is needed
    val group_volume: Int? = null,
    val display_name: String,
    val icon: String? = null,
    val power_control: String? = null,
    val volume_control: String? = null
    // Skipping synced_to, active_group, hide_player_in_ui, mute_control for initial simplicity
)

@Serializable
data class QueueItem(
    val queue_id: String,
    val queue_item_id: String,
    val name: String,
    val duration: Float, // Changed from Int to Float for decimal durations
    val sort_index: Int,
    val streamdetails: StreamDetails? = null,
    val media_item: PlayableMediaItem? = null, // Using our sealed class
    val image_url: String? = null, // Fallback if media_item.metadata.images is not available
    val available: Boolean = true // Default to true, server will dictate
)

@Serializable
data class PlayerQueue(
    val queue_id: String,
    val active: Boolean,
    val display_name: String,
    val available: Boolean,
    val items: Int, // Total count of items
    val shuffle_enabled: Boolean,
    val repeat_mode: RepeatMode, // Enum
    val current_index: Int? = null,
    val elapsed_time: Float, // Using Float for more precision
    val elapsed_time_last_updated: Double, // Changed from Long to Double for decimal timestamps
    val state: PlayerState, // Enum
    val current_item: QueueItem? = null,
    val next_item: QueueItem? = null
    // Skipping radio_source, dont_stop_the_music_enabled, index_in_buffer for simplicity
) 