package com.mass.client.core.network

import com.mass.client.core.model.Player
import com.mass.client.core.model.PlayerQueue
import com.mass.client.core.model.QueueItem
import com.mass.client.core.model.QueueOption
import com.mass.client.core.model.RepeatMode
import com.mass.client.core.model.Track
import com.mass.client.core.model.Artist
import com.mass.client.core.model.Album
import com.mass.client.core.model.ItemMapping
import com.mass.client.core.model.MediaType
import com.mass.client.feature_home.model.UiPlayer
import com.mass.client.feature_home.model.toUiPlayer
import io.music_assistant.client.data.MainDataSource
import io.music_assistant.client.data.model.server.ServerMediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import io.ktor.http.encodeURLQueryComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.builtins.ListSerializer

@OptIn(ExperimentalCoroutinesApi::class)
class ApiServiceImpl(
    private val webSocketClient: WebSocketClient,
    private val json: Json,
    private val mainDataSource: MainDataSource
) : ApiService {

    // CoroutineScope for stateIn operator
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    override val players: StateFlow<List<UiPlayer>> =
        mainDataSource.playersData
            .map { playerDataList ->
                playerDataList.map { playerData ->
                    playerData.toUiPlayer()
                }
            }
            .stateIn(
                scope = serviceScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Helper to construct args map with JsonElement values
    private fun jsonArgs(vararg pairs: Pair<String, Any?>): Map<String, JsonElement?>? {
        val map = mutableMapOf<String, JsonElement?>()
        for ((key, value) in pairs) {
            when (value) {
                null -> map[key] = JsonNull
                is String -> map[key] = JsonPrimitive(value)
                is Number -> map[key] = JsonPrimitive(value)
                is Boolean -> map[key] = JsonPrimitive(value)
                is List<*> -> {
                    // Assuming lists are of primitives for now, primarily strings (URIs)
                    map[key] = buildJsonArray {
                        value.forEach { item ->
                            when (item) {
                                is String -> add(JsonPrimitive(item))
                                is Number -> add(JsonPrimitive(item))
                                is Boolean -> add(JsonPrimitive(item))
                                // Add other primitive list types if needed
                                else -> add(JsonNull) // Or throw exception for unsupported list item type
                            }
                        }
                    }
                }
                // Add more complex type handling to JsonElement if necessary
                else -> {
                    // For unknown types, either serialize to a string or handle as an error.
                    // Direct serialization of Any? to JsonElement is not straightforward without context.
                    // For now, converting to string and wrapping in JsonPrimitive, or using JsonNull.
                    println("ApiServiceImpl: WARN - jsonArgs encountered an unhandled type for key '$key': ${value?.let { it::class.simpleName } ?: "null"}. Storing as JsonNull.")
                    map[key] = JsonNull // Or JsonPrimitive(value.toString()) if that's desired and safe
                }
            }
        }
        return if (map.isNotEmpty()) map else null
    }

    override suspend fun playerCommandPlayPause(playerId: String) {
        webSocketClient.awaitConnectionReady()
        webSocketClient.sendCommand("players/cmd/play_pause", jsonArgs("player_id" to playerId))
    }

    override suspend fun playerCommandStop(playerId: String) {
        webSocketClient.awaitConnectionReady()
        webSocketClient.sendCommand("players/cmd/stop", jsonArgs("player_id" to playerId))
    }

    override suspend fun playerCommandNext(playerId: String) {
        webSocketClient.awaitConnectionReady()
        webSocketClient.sendCommand("players/cmd/next", jsonArgs("player_id" to playerId))
    }

    override suspend fun playerCommandPrevious(playerId: String) {
        webSocketClient.awaitConnectionReady()
        webSocketClient.sendCommand("players/cmd/previous", jsonArgs("player_id" to playerId))
    }

    override suspend fun playerCommandSeek(playerId: String, positionSeconds: Int) {
        webSocketClient.awaitConnectionReady()
        webSocketClient.sendCommand("players/cmd/seek", jsonArgs("player_id" to playerId, "position" to positionSeconds))
    }

    override suspend fun playerCommandPowerToggle(playerId: String) {
        webSocketClient.awaitConnectionReady()
        println("ApiServiceImpl: WARN - playerCommandPowerToggle needs current player state or a dedicated server toggle command. Assuming POWER ON for now.")
        webSocketClient.sendCommand("players/cmd/power", jsonArgs("player_id" to playerId, "powered" to true))
    }

    override suspend fun playerCommandVolumeSet(playerId: String, volumeLevel: Int) {
        webSocketClient.awaitConnectionReady()
        webSocketClient.sendCommand("players/cmd/volume_set", jsonArgs("player_id" to playerId, "volume_level" to volumeLevel))
    }

    override suspend fun queueCommandShuffle(queueId: String, shuffleEnabled: Boolean) {
        webSocketClient.awaitConnectionReady()
        webSocketClient.sendCommand("player_queues/shuffle", jsonArgs("queue_id" to queueId, "shuffle_enabled" to shuffleEnabled))
    }

    override suspend fun queueCommandRepeat(queueId: String, repeatMode: RepeatMode) {
        webSocketClient.awaitConnectionReady()
        webSocketClient.sendCommand("player_queues/repeat", jsonArgs("queue_id" to queueId, "repeat_mode" to repeatMode.name.lowercase()))
    }

    override suspend fun queueCommandPlayIndex(queueId: String, index: Int) {
        webSocketClient.awaitConnectionReady()
        webSocketClient.sendCommand("player_queues/play_index", jsonArgs("queue_id" to queueId, "index" to index))
    }

    override suspend fun queueCommandClear(queueId: String) {
        webSocketClient.awaitConnectionReady()
        webSocketClient.sendCommand("player_queues/clear", jsonArgs("queue_id" to queueId))
    }

    override suspend fun playMedia(
        queueId: String,
        media: List<String>,
        option: QueueOption?,
        radioMode: Boolean?,
        startItemId: String?
    ) {
        webSocketClient.awaitConnectionReady()
        webSocketClient.sendCommand("player_queues/play_media", jsonArgs(
            "queue_id" to queueId,
            "media" to media, // jsonArgs helper will turn this into a JsonArray of JsonPrimitives
            "option" to option?.name?.lowercase(),
            "radio_mode" to radioMode,
            "start_item" to startItemId
        ))
    }

    override suspend fun getLibraryTracks(
        favorite: Boolean?,
        search: String?,
        limit: Int?,
        offset: Int?,
        orderBy: String?
    ): List<Track> {
        webSocketClient.awaitConnectionReady()
        val resultElement = webSocketClient.sendCommandAndWaitForResponse("music/tracks/library_items", jsonArgs(
            "favorite" to favorite,
            "search" to search,
            "limit" to limit,
            "offset" to offset,
            "order_by" to orderBy
        ))
        return resultElement?.let { json.decodeFromJsonElement(ListSerializer(Track.serializer()), it) } ?: emptyList()
    }

    override suspend fun getLibraryArtists(
        favorite: Boolean?,
        search: String?,
        limit: Int?,
        offset: Int?,
        orderBy: String?
    ): List<Artist> {
        webSocketClient.awaitConnectionReady()
        val resultElement = webSocketClient.sendCommandAndWaitForResponse("music/artists/library_items", jsonArgs(
            "favorite" to favorite,
            "search" to search,
            "limit" to limit,
            "offset" to offset,
            "order_by" to orderBy
        ))
        // Assuming the server endpoint for artists is "music/artists"
        // and it supports these parameters. This needs to be verified with the server API.
        return resultElement?.let { json.decodeFromJsonElement(ListSerializer(Artist.serializer()), it) } ?: emptyList()
    }

    override suspend fun getLibraryAlbums(
        favorite: Boolean?,
        search: String?,
        limit: Int?,
        offset: Int?,
        orderBy: String?
    ): List<Album> {
        webSocketClient.awaitConnectionReady()
        val resultElement = webSocketClient.sendCommandAndWaitForResponse("music/albums/library_items", jsonArgs(
            "favorite" to favorite,
            "search" to search,
            "limit" to limit,
            "offset" to offset,
            "order_by" to orderBy
        ))
        // Assuming the server endpoint for albums is "music/albums"
        // and it supports these parameters. This needs to be verified with the server API.
        return resultElement?.let { json.decodeFromJsonElement(ListSerializer(Album.serializer()), it) } ?: emptyList()
    }

    override suspend fun getAllPlayers(): List<Player> {
        webSocketClient.awaitConnectionReady()
        val resultElement = webSocketClient.sendCommandAndWaitForResponse("players/all", null)
        return resultElement?.let { json.decodeFromJsonElement(ListSerializer(Player.serializer()), it) } ?: emptyList()
    }

    override suspend fun getAllPlayerQueues(): List<PlayerQueue> {
        webSocketClient.awaitConnectionReady()
        val resultElement = webSocketClient.sendCommandAndWaitForResponse("player_queues/all", null)
        return resultElement?.let { json.decodeFromJsonElement(ListSerializer(PlayerQueue.serializer()), it) } ?: emptyList()
    }

    override suspend fun getQueueItems(queueId: String, limit: Int, offset: Int): List<QueueItem> {
        webSocketClient.awaitConnectionReady()
        val resultElement = webSocketClient.sendCommandAndWaitForResponse("player_queues/items", jsonArgs(
            "queue_id" to queueId,
            "limit" to limit,
            "offset" to offset
        ))
        return resultElement?.let { json.decodeFromJsonElement(ListSerializer(QueueItem.serializer()), it) } ?: emptyList()
    }

    override suspend fun getRecentlyPlayedItems(
        limit: Int,
        mediaTypes: List<com.mass.client.core.model.MediaType>?
    ): List<ItemMapping> {
        webSocketClient.awaitConnectionReady()
        val args = mutableMapOf<String, Any?>("limit" to limit)
        mediaTypes?.let {
            args["media_types"] = it.map { type -> type.name.lowercase() }
        }
        println("#### ApiServiceImpl: Sending command music/recently_played_items with args: $args")
        try {
            val resultElement = webSocketClient.sendCommandAndWaitForResponse(
                "music/recently_played_items", // Correct endpoint
                jsonArgs(*args.toList().toTypedArray())
            )
            println("#### ApiServiceImpl: music/recently_played_items RAW RESPONSE: $resultElement")
            if (resultElement == null) {
                println("#### ApiServiceImpl: music/recently_played_items - Received NULL resultElement from sendCommandAndWaitForResponse")
                return emptyList()
            }
            // Decode into ItemMapping
            val items = json.decodeFromJsonElement(ListSerializer(ItemMapping.serializer()), resultElement)

            val baseUrl = try {
                webSocketClient.serverInfo.first().base_url
            } catch (e: Exception) {
                println("#### ApiServiceImpl: Could not get baseUrl from webSocketClient.serverInfo: ${e.message}")
                null
            }

            return if (baseUrl != null) {
                items.map { item ->
                    item.image?.let { img ->
                        if (img.path.isNotBlank()) {
                            if (!img.remotely_accessible || !img.path.startsWith("http")) {
                                // Path needs proxying
                                val encodedPath =   img.path.encodeURLQueryComponent()
                                val provider = img.provider.encodeURLQueryComponent() // Provider name might also need encoding if it contains special chars
                                val proxiedUrl = "$baseUrl/imageproxy?path=$encodedPath&provider=$provider"
                                println("#### ApiServiceImpl: Item: ${item.name}, Original path: ${img.path}, Provider: ${img.provider}, RemotelyAccessible: ${img.remotely_accessible} -> Using PROXY URL: $proxiedUrl")
                                item.copy(image = img.copy(path = proxiedUrl))
                            } else {
                                // Path is already absolute and remotely accessible
                                println("#### ApiServiceImpl: Item: ${item.name}, Path already absolute and remotely accessible: ${img.path}")
                                item
                            }
                        } else {
                            // Image path is blank
                            println("#### ApiServiceImpl: Item: ${item.name}, Image path is blank.")
                            item
                        }
                    } ?: item // No image to process
                }
            } else {
                println("#### ApiServiceImpl: baseUrl was null, returning items as is.")
                items
            }
        } catch (e: Exception) {
            println("#### ApiServiceImpl: ERROR in getRecentlyPlayedItems for music/recently_played_items: ${e.message}")
            println("#### ApiServiceImpl: Exception details: ${e.toString()}")
            return emptyList()
        }
    }
} 