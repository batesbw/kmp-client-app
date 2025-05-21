package com.mass.client.core.model

import kotlinx.serialization.Serializable

// Based on frontend/src/plugins/api/interfaces.ts

@Serializable
data class MediaItemImage(
    val type: ImageType, // Enum
    val path: String,
    val provider: String,
    val remotely_accessible: Boolean
)

@Serializable
data class MediaItemMetadata(
    val description: String? = null,
    val explicit: Boolean? = null,
    val images: List<MediaItemImage>? = null,
    val genres: List<String>? = null,
    val lyrics: String? = null, // Plain text lyrics
    val lrc_lyrics: String? = null, // LRC format lyrics
    // Add other metadata fields as needed: review, mood, style, copyright, label, links, performers, preview, replaygain, popularity, chapters
)

@Serializable
data class ProviderMapping(
    val item_id: String,
    val provider_domain: String,
    val provider_instance: String,
    val available: Boolean,
    val audio_format: AudioFormat? = null, // Defined in Audio.kt
    val url: String? = null
)

// Base for all media items
@Serializable
sealed class MediaItem {
    abstract val item_id: String
    abstract val provider: String
    abstract val name: String
    abstract val version: String?
    abstract val uri: String
    abstract val media_type: MediaType // Enum
    abstract val is_playable: Boolean
    abstract val provider_mappings: List<ProviderMapping>?
    abstract val metadata: MediaItemMetadata?
    abstract val favorite: Boolean?
}

// Specific Media Item Types
@Serializable
data class Track(
    override val item_id: String,
    override val provider: String,
    override val name: String,
    override val version: String? = null,
    override val uri: String,
    override val media_type: MediaType = MediaType.TRACK,
    override val is_playable: Boolean = true,
    override val provider_mappings: List<ProviderMapping>? = null,
    override val metadata: MediaItemMetadata? = null,
    override val favorite: Boolean? = null,

    val duration: Int, // seconds
    val artists: List<Artist>? = null, // Simplified to avoid circular refs with full Artist, could be ItemMapping
    val album: Album? = null, // Simplified, could be ItemMapping
    val disc_number: Int? = null,
    val track_number: Int? = null
) : MediaItem()

@Serializable
data class Album(
    override val item_id: String,
    override val provider: String,
    override val name: String,
    override val version: String? = null,
    override val uri: String,
    override val media_type: MediaType = MediaType.ALBUM,
    override val is_playable: Boolean = false, // Albums themselves aren't directly played, their tracks are
    override val provider_mappings: List<ProviderMapping>? = null,
    override val metadata: MediaItemMetadata? = null,
    override val favorite: Boolean? = null,

    val year: Int? = null,
    val artists: List<Artist>? = null, // Simplified
    val album_type: AlbumType // Enum
) : MediaItem()

@Serializable
data class Artist(
    override val item_id: String,
    override val provider: String,
    override val name: String,
    override val version: String? = null,
    override val uri: String,
    override val media_type: MediaType = MediaType.ARTIST,
    override val is_playable: Boolean = false, // Artists themselves aren't directly played
    override val provider_mappings: List<ProviderMapping>? = null,
    override val metadata: MediaItemMetadata? = null,
    override val favorite: Boolean? = null
) : MediaItem()

// ItemMapping: A lightweight reference, can be expanded if needed.
// For now, full items are preferred in the models above for simplicity where possible.
@Serializable
data class ItemMapping(
    val item_id: String,
    val provider: String,
    val name: String,
    val version: String? = null,
    val uri: String,
    val media_type: MediaType, // Enum
    val available: Boolean,
    val image: MediaItemImage? = null
)

// Simplified PlayableMediaItemType for QueueItem
// Can be one of Track, Radio (to be defined), etc.
// For now, we'll assume QueueItem.media_item can be a Track.
// More specific types like Radio, PodcastEpisode can be added here when needed.
@Serializable
sealed class PlayableMediaItem : MediaItem()

// Extend Track to be a PlayableMediaItem
// No new fields needed, just inherits and satisfies the sealed class for type safety
@Serializable
data class PlayableTrack(
    val track: Track
) : PlayableMediaItem() {
    override val item_id: String get() = track.item_id
    override val provider: String get() = track.provider
    override val name: String get() = track.name
    override val version: String? get() = track.version
    override val uri: String get() = track.uri
    override val media_type: MediaType get() = track.media_type
    override val is_playable: Boolean get() = track.is_playable
    override val provider_mappings: List<ProviderMapping>? get() = track.provider_mappings
    override val metadata: MediaItemMetadata? get() = track.metadata
    override val favorite: Boolean? get() = track.favorite
}

// TODO: Define Radio, Playlist, Audiobook, Podcast, PodcastEpisode, BrowseFolder when needed 