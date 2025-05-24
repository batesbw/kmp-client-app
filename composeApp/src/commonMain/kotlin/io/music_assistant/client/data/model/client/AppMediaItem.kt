package io.music_assistant.client.data.model.client

import com.mass.client.core.model.MediaType
import io.music_assistant.client.data.model.server.Metadata
import io.music_assistant.client.data.model.server.ServerMediaItem
import io.music_assistant.client.data.model.server.MediaType as ServerMediaType

// Convert old MediaType to new MediaType
private fun ServerMediaType.toNewMediaType(): MediaType = when (this) {
    ServerMediaType.ARTIST -> MediaType.artist
    ServerMediaType.ALBUM -> MediaType.album
    ServerMediaType.TRACK -> MediaType.track
    ServerMediaType.PLAYLIST -> MediaType.playlist
    ServerMediaType.RADIO -> MediaType.radio
    ServerMediaType.AUDIOBOOK -> MediaType.audiobook
    ServerMediaType.PODCAST -> MediaType.podcast
    ServerMediaType.PODCAST_EPISODE -> MediaType.podcast_episode
    ServerMediaType.FOLDER -> MediaType.folder
    ServerMediaType.UNKNOWN -> MediaType.unknown
    else -> MediaType.unknown // fallback for any other values
}

abstract class AppMediaItem(
    val itemId: String,
    val provider: String,
    val name: String,
    //val providerMappings: List<ProviderMapping>?,
    metadata: Metadata?,
    //val favorite: Boolean?,
    val mediaType: MediaType,
    //val sortName: String?,
    val uri: String?,
    //val isPlayable: Boolean?,
    //val timestampAdded: Long?,
    //val timestampModified: Long?,
) {

    open val subtitle: String? = null
    val longId = itemId.hashCode().toLong()

    override fun equals(other: Any?): Boolean {
        return other is AppMediaItem
                && itemId == other.itemId
                && name == other.name
                && mediaType == other.mediaType
                && provider == other.provider
    }

    override fun hashCode(): Int {
        return mediaType.hashCode() +
                19 * itemId.hashCode() +
                31 * provider.hashCode() +
                37 * name.hashCode()
    }

    val imageUrl: String? = metadata?.images?.getOrNull(0)?.path

    class Artist(
        itemId: String,
        provider: String,
        name: String,
//        providerMappings: List<ProviderMapping>?,
        metadata: Metadata?,
//        favorite: Boolean?,
//        mediaType: MediaType,
        //sortName: String?,
        uri: String?,
//        isPlayable: Boolean?,
//        timestampAdded: Long?,
//        timestampModified: Long?,
//        val musicbrainzId: String?,
    ) : AppMediaItem(
        itemId,
        provider,
        name,
        //providerMappings,
        metadata,
        //favorite,
        MediaType.artist,
        //sortName,
        uri,
        //isPlayable,
        //timestampAdded,
        //timestampModified,
    )

    class Album(
        itemId: String,
        provider: String,
        name: String,
//        providerMappings: List<ProviderMapping>?,
        metadata: Metadata?,
//        favorite: Boolean?,
//        mediaType: MediaType,
//        sortName: String?,
        uri: String?,
//        isPlayable: Boolean?,
//        timestampAdded: Long?,
//        timestampModified: Long?,
//        val musicbrainzId: String?,
        //val version: String?,
//        val year: Int?,
        val artists: List<Artist>?,
//        val albumType: AlbumType?,
    ) : AppMediaItem(
        itemId,
        provider,
        name,
        //providerMappings,
        metadata,
        //favorite,
        MediaType.album,
        //sortName,
        uri,
        //isPlayable,
        //timestampAdded,
        //timestampModified,
    ) {
        override val subtitle = "Album - ${artists?.joinToString(separator = ", ") { it.name }}"
    }

    class Track(
        itemId: String,
        provider: String,
        name: String,
//        providerMappings: List<ProviderMapping>?,
        metadata: Metadata?,
//        favorite: Boolean?,
//        mediaType: MediaType,
//        sortName: String?,
        uri: String?,
//        isPlayable: Boolean?,
//        timestampAdded: Long?,
//        timestampModified: Long?,
//        val musicbrainzId: String?,
        //val version: String?,
        val duration: Double?,
//        val isrc: String?,
        val artists: List<Artist>?,
// album track only
//        val album: Album?,
//        val discNumber: Int?,
//        val trackNumber: Int?,
// playlist track only
//        val position: Int?,
    ) : AppMediaItem(
        itemId,
        provider,
        name,
        //providerMappings,
        metadata,
        //favorite,
        MediaType.track,
        //sortName,
        uri,
        //isPlayable,
        //timestampAdded,
        //timestampModified,
    ) {
        override val subtitle = artists?.joinToString(separator = ", ") { it.name }
        val description =
            "${artists?.joinToString(separator = ", ") { it.name } ?: "Unknown"} - $name"
    }

    class Playlist(
        itemId: String,
        provider: String,
        name: String,
        //providerMappings: List<ProviderMapping>?,
        metadata: Metadata?,
        //favorite: Boolean?,
        //mediaType: MediaType,
        //sortName: String?,
        uri: String?,
        //isPlayable: Boolean?,
        //timestampAdded: Long?,
        //timestampModified: Long?,
        //val owner: String?,
        //val isEditable: Boolean?,
    ) : AppMediaItem(
        itemId,
        provider,
        name,
        //providerMappings,
        metadata,
        //favorite,
        MediaType.playlist,
        //sortName,
        uri,
        //isPlayable,
        //timestampAdded,
        //timestampModified,
    ) {
        override val subtitle = "Playlist"
    }

    companion object {
        fun ServerMediaItem.toAppMediaItem(): AppMediaItem? =
            when (mediaType.toNewMediaType()) {
                MediaType.artist -> Artist(
                    itemId = itemId,
                    provider = provider,
                    name = name,
                    metadata = metadata,
                    uri = uri,
                )

                MediaType.album -> Album(
                    itemId = itemId,
                    provider = provider,
                    name = name,
                    metadata = metadata,
                    uri = uri,
                    artists = artists?.mapNotNull { it.toAppMediaItem() as? Artist },
                )

                MediaType.track -> Track(
                    itemId = itemId,
                    provider = provider,
                    name = name,
                    metadata = metadata,
                    uri = uri,
                    duration = duration,
                    artists = artists?.mapNotNull { it.toAppMediaItem() as? Artist },
                )

                MediaType.playlist -> Playlist(
                    itemId = itemId,
                    provider = provider,
                    name = name,
                    metadata = metadata,
                    uri = uri,
                )

                MediaType.radio,
                MediaType.audiobook,
                MediaType.podcast,
                MediaType.podcast_episode,
                MediaType.folder,
                MediaType.unknown -> null
                
                else -> null // Handle any other cases
            }

        fun List<ServerMediaItem>.toAppMediaItemList() =
            mapNotNull { it.toAppMediaItem() }
    }

// TODO Radio, audiobooks, podcasts
}

