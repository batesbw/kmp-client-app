package io.music_assistant.client.auto

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import androidx.annotation.DrawableRes
import androidx.media.MediaBrowserServiceCompat
import androidx.media.utils.MediaConstants
import io.music_assistant.client.R
import io.music_assistant.client.api.ServiceClient
import io.music_assistant.client.api.getAlbumTracksRequest
import io.music_assistant.client.api.getArtistAlbumsRequest
import io.music_assistant.client.api.getArtistsRequest
import io.music_assistant.client.api.getPlaylistsRequest
import io.music_assistant.client.api.playMediaRequest
import io.music_assistant.client.api.searchRequest
import io.music_assistant.client.data.model.client.AppMediaItem
import io.music_assistant.client.data.model.client.AppMediaItem.Companion.toAppMediaItem
import io.music_assistant.client.data.model.client.AppMediaItem.Companion.toAppMediaItemList
import com.mass.client.core.model.MediaType
import io.music_assistant.client.data.model.server.QueueOption
import io.music_assistant.client.data.model.server.SearchResult
import io.music_assistant.client.data.model.server.ServerMediaItem
import io.music_assistant.client.data.model.server.MediaType as ServerMediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@OptIn(FlowPreview::class)
class AutoLibrary(
    private val context: Context,
    private val apiClient: ServiceClient,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val searchFlow: MutableStateFlow<Pair<String, MediaBrowserServiceCompat.Result<List<MediaItem>>>?> =
        MutableStateFlow(null)
    private val defaultIconUri = R.drawable.baseline_library_music_24.toUri(context)

    init {
        scope.launch {
            searchFlow
                .filterNotNull()
                .filter { it.first.isNotEmpty() }
                .debounce(500)
                .collect { (query, result) ->
                    val answer = apiClient.sendRequest(
                        searchRequest(
                            query,
                            listOf(
                                MediaType.artist,
                                MediaType.album,
                                MediaType.track,
                                MediaType.playlist
                            ).map { it.toServerMediaType() }
                        )
                    )
                    answer?.resultAs<SearchResult>()?.let {
                        result.sendResult(it.toAutoMediaItems(defaultIconUri))
                    } ?: result.sendResult(null)
                }
        }
    }

    fun getItems(
        id: String,
        result: MediaBrowserServiceCompat.Result<List<MediaItem>>
    ) {
        println("Auto: items for $id")
        when (id) {
            MediaIds.ROOT -> {
                result.sendResult(
                    listOf(
                        rootTabItem("Artists", MediaIds.TAB_ARTISTS),
                        rootTabItem("Playlists", MediaIds.TAB_PLAYLISTS)
                    )
                )
            }

            MediaIds.TAB_ARTISTS -> {
                result.detach()
                scope.launch {
                    result.sendResult(apiClient.sendRequest(getArtistsRequest())
                        ?.resultAs<List<ServerMediaItem>>()
                        ?.toAppMediaItemList()?.map { it.toAutoMediaItem(true, defaultIconUri) })
                }
            }

            MediaIds.TAB_PLAYLISTS -> {
                result.detach()
                scope.launch {
                    result.sendResult(apiClient.sendRequest(getPlaylistsRequest())
                        ?.resultAs<List<ServerMediaItem>>()
                        ?.toAppMediaItemList()?.map { it.toAutoMediaItem(true, defaultIconUri) })
                }
            }

            else -> {
                val parts = id.split("__")
                if (parts.size != 4) {
                    result.sendResult(null)
                    return
                }
                result.detach()
                val parentType = MediaType.valueOf(parts[2].lowercase())
                val requestAndCategory = when (parentType) {
                    MediaType.artist -> getArtistAlbumsRequest(parts[0], parts[3])
                    MediaType.album -> getAlbumTracksRequest(parts[0], parts[3])
                    else -> {
                        result.sendResult(null)
                        return
                    }
                }
                scope.launch {
                    val list = apiClient.sendRequest(requestAndCategory)
                        ?.resultAs<List<ServerMediaItem>>()
                        ?.toAppMediaItemList()?.map { it.toAutoMediaItem(true, defaultIconUri) }
                    result.sendResult(list?.let { actionsForItem(id) + it })
                }
            }
        }
    }

    private fun actionsForItem(itemId: String): List<MediaItem> {
        return buildList {
            add(
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setTitle("Play all")
                        .setMediaId(itemId)
                        .setIconUri(android.R.drawable.ic_media_play.toUri(context))
                        .setExtras(Bundle().apply {
                            putString(
                                MediaIds.QUEUE_OPTION_KEY,
                                QueueOption.REPLACE.name
                            )
                        })
                        .build(),
                    MediaItem.FLAG_PLAYABLE
                )
            )
            add(
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setTitle("Add all to queue")
                        .setMediaId(itemId)
                        .setIconUri(android.R.drawable.ic_menu_add.toUri(context))
                        .setExtras(Bundle().apply {
                            putString(
                                MediaIds.QUEUE_OPTION_KEY,
                                QueueOption.ADD.name
                            )
                        })
                        .build(),
                    MediaItem.FLAG_PLAYABLE
                )
            )
        }
    }

    fun search(
        query: String,
        result: MediaBrowserServiceCompat.Result<List<MediaItem>>
    ) {
        result.detach()
        // converting to flow for filtering and debouncing
        searchFlow.update { Pair(query, result) }
    }

    fun play(id: String, extras: Bundle?, queueId: String) {
        id.split("__").getOrNull(1)?.let { uri ->
            scope.launch {
                apiClient.sendRequest(
                    playMediaRequest(
                        media = listOf(uri),
                        queueOrPlayerId = queueId,
                        option = extras?.getString(
                            MediaIds.QUEUE_OPTION_KEY,
                            QueueOption.PLAY.name
                        )?.let { QueueOption.valueOf(it) },
                        radioMode = false
                    )
                )
            }
        }
    }

    private fun rootTabItem(tabName: String, tabId: String): MediaItem =
        MediaItem(
            MediaDescriptionCompat.Builder()
                .setTitle(tabName)
                .setMediaId(tabId)
                .build(), MediaItem.FLAG_BROWSABLE
        )
}


internal object MediaIds {
    const val ROOT = "auto_lib_root"
    const val TAB_ARTISTS = "auto_lib_artists"
    const val TAB_PLAYLISTS = "auto_lib_playlists"
    const val QUEUE_OPTION_KEY = "auto_queue_option"
    const val ARTIST_ACTIONS = "artists_actions"
    const val ALBUM_ACTIONS = "album_actions"
    const val CATEGORY_KEY = "category_key"
    const val PLAYABLE_PREFIX = "playable_"
}

private fun SearchResult.toAutoMediaItems(defaultIconUri: Uri): List<MediaItem> = buildList {
    artists?.let { list -> addAll(list.toAppMediaItemList().map { it.toAutoMediaItem(true, defaultIconUri) }) }
    albums?.let { list -> addAll(list.toAppMediaItemList().map { it.toAutoMediaItem(true, defaultIconUri) }) }
    tracks?.let { list -> addAll(list.toAppMediaItemList().map { it.toAutoMediaItem(false, defaultIconUri) }) }
    playlists?.let { list -> addAll(list.toAppMediaItemList().map { it.toAutoMediaItem(true, defaultIconUri) }) }
}

private fun ServerMediaItem.toAutoMediaItem(
    allowBrowse: Boolean,
    defaultIconUri: Uri,
    category: String? = null
): MediaItem? =
    toAppMediaItem()?.toAutoMediaItem(allowBrowse, defaultIconUri, category)

private fun AppMediaItem.toAutoMediaItem(
    allowBrowse: Boolean,
    defaultIconUri: Uri,
    category: String? = null
): MediaItem {
    val extras = Bundle()
    extras.putString(MediaIds.CATEGORY_KEY, mediaType.name.lowercase())
    val id = if(allowBrowse) itemId else "${MediaIds.PLAYABLE_PREFIX}${itemId}"
    return MediaItem(
        MediaDescriptionCompat.Builder()
            .setTitle(name)
            .setSubtitle(subtitle)
            .setMediaId(id)
            .setIconUri(imageUrl?.let { Uri.parse(it) } ?: defaultIconUri)
            .setExtras(extras)
            .build(),
        if (allowBrowse) MediaItem.FLAG_BROWSABLE else MediaItem.FLAG_PLAYABLE
    )
}

private fun MediaType.isBrowsableInAuto(): Boolean = this in setOf(
    MediaType.artist, MediaType.album
)

fun @receiver:DrawableRes Int.toUri(context: Context): Uri = Uri.parse(
    ContentResolver.SCHEME_ANDROID_RESOURCE
            + "://" + context.resources.getResourcePackageName(this)
            + '/' + context.resources.getResourceTypeName(this)
            + '/' + context.resources.getResourceEntryName(this)
)

fun AppMediaItem.toMediaDescription(
    defaultIconUri: Uri,
    category: String? = null
) = MediaDescriptionCompat.Builder()
    .setMediaId("${itemId}__${uri}__${mediaType}__${provider}")
    .setTitle(name)
    .setSubtitle(subtitle)
    .setMediaUri(Uri.parse(uri))
    .setIconUri(imageUrl?.let { Uri.parse(it) } ?: defaultIconUri)
    .setExtras(Bundle().apply {
        putString(
            MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_GROUP_TITLE,
            category
        )
    })
    .build()

private fun MediaType.toServerMediaType(): ServerMediaType = when (this) {
    MediaType.artist -> ServerMediaType.ARTIST
    MediaType.album -> ServerMediaType.ALBUM
    MediaType.track -> ServerMediaType.TRACK
    MediaType.playlist -> ServerMediaType.PLAYLIST
    MediaType.radio -> ServerMediaType.RADIO
    MediaType.audiobook -> ServerMediaType.AUDIOBOOK
    MediaType.podcast -> ServerMediaType.PODCAST
    MediaType.podcast_episode -> ServerMediaType.PODCAST_EPISODE
    MediaType.folder -> ServerMediaType.FOLDER
    MediaType.unknown -> ServerMediaType.UNKNOWN
}
