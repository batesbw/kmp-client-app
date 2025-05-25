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
import kotlinx.coroutines.flow.combine

@OptIn(ExperimentalCoroutinesApi::class)
class ApiServiceImpl(
    private val webSocketClient: WebSocketClient,
    private val json: Json,
    private val mainDataSource: MainDataSource
) : ApiService {

    private val serviceScope = CoroutineScope(Dispatchers.Default)

    override val players: StateFlow<List<UiPlayer>> =
        mainDataSource.playersData.combine(mainDataSource.selectedPlayerData) { playerDataList, selectedPlayerInfo ->
            val activePlayerId = selectedPlayerInfo?.playerId
            playerDataList.map { playerData ->
                playerData.toUiPlayer(activePlayerId)
            }
        }
        .stateIn(
            scope = serviceScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun jsonArgs(vararg pairs: Pair<String, Any?>): Map<String, JsonElement?>? {
        val map = mutableMapOf<String, JsonElement?>()
        for ((key, value) in pairs) {
            when (value) {
                null -> map[key] = JsonNull
                is String -> map[key] = JsonPrimitive(value)
                is Number -> map[key] = JsonPrimitive(value)
                is Boolean -> map[key] = JsonPrimitive(value)
                is List<*> -> {
                    map[key] = buildJsonArray {
                        value.forEach {
                            when (it) {
                                is String -> add(JsonPrimitive(it))
                                is Number -> add(JsonPrimitive(it))
                                is Boolean -> add(JsonPrimitive(it))
                                else -> add(JsonNull)
                            }
                        }
                    }
                }
                else -> {
                    println("ApiServiceImpl: WARN - jsonArgs unhandled type for key '$key': ${value::class.simpleName}. Storing as JsonNull.")
                    map[key] = JsonNull
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
            "media" to media,
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

    override suspend fun getLibrarySyncStatus(): JsonElement? {
        webSocketClient.awaitConnectionReady()
        println("ApiServiceImpl: Fetching library sync status...")
        return try {
            webSocketClient.sendCommandAndWaitForResponse("music/syncstatus", null)
        } catch (e: Exception) {
            println("ApiServiceImpl: Error calling music/syncstatus: ${e.message}")
            null
        }
    }

    override suspend fun getRecentlyPlayedItems(
        limit: Int,
        mediaTypes: List<MediaType>?
    ): List<ItemMapping> {
        webSocketClient.awaitConnectionReady()
        val args = mutableMapOf<String, Any?>("limit" to limit)
        mediaTypes?.let {
            args["media_types"] = it.map { type -> type.name.lowercase() }
        }
        println("#### ApiServiceImpl: Sending command music/recently_played_items with args: $args")
        try {
            val resultElement = webSocketClient.sendCommandAndWaitForResponse(
                "music/recently_played_items",
                jsonArgs(*args.toList().toTypedArray())
            )
            println("#### ApiServiceImpl: music/recently_played_items RAW RESPONSE: $resultElement")
            if (resultElement == null) {
                println("#### ApiServiceImpl: music/recently_played_items - Received NULL resultElement")
                return emptyList()
            }
            val items = json.decodeFromJsonElement(ListSerializer(ItemMapping.serializer()), resultElement)

            val baseUrl = try {
                webSocketClient.serverInfo.first().base_url
            } catch (e: Exception) {
                println("#### ApiServiceImpl: Could not get baseUrl: ${e.message}")
                null
            }

            return if (baseUrl != null) {
                items.map { item ->
                    item.image?.let { img ->
                        if (img.path.isNotBlank()) {
                            if (!img.remotely_accessible || !img.path.startsWith("http")) {
                                val encodedPath = img.path.encodeURLQueryComponent()
                                val provider = img.provider.encodeURLQueryComponent()
                                val proxiedUrl = "$baseUrl/imageproxy?path=$encodedPath&provider=$provider"
                                println("#### ApiServiceImpl: Item: ${item.name} -> PROXY URL: $proxiedUrl")
                                item.copy(image = img.copy(path = proxiedUrl))
                            } else {
                                item
                            }
                        } else {
                            item
                        }
                    } ?: item
                }
            } else {
                items
            }
        } catch (e: Exception) {
            println("#### ApiServiceImpl: ERROR in getRecentlyPlayedItems: ${e.message}")
            return emptyList()
        }
    }

    override suspend fun getCoreConfigValue(domain: String, key: String): JsonElement? {
        webSocketClient.awaitConnectionReady()
        val args = jsonArgs("domain" to domain, "key" to key)
        println("#### ApiServiceImpl: Sending command config/core/get_value with args: $args")
        return try {
            webSocketClient.sendCommandAndWaitForResponse(
                "config/core/get_value",
                args
            )
        } catch (e: Exception) {
            println("ApiServiceImpl: Error calling config/core/get_value for domain=$domain, key=$key: ${e.message}")
            null
        }
    }
} 