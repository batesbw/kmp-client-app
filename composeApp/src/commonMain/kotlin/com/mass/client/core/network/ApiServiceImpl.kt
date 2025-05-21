package com.mass.client.core.network

import com.mass.client.core.model.Player
import com.mass.client.core.model.PlayerQueue
import com.mass.client.core.model.QueueItem
import com.mass.client.core.model.QueueOption
import com.mass.client.core.model.RepeatMode
import com.mass.client.core.model.Track
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonObject

class ApiServiceImpl(
    private val webSocketClient: WebSocketClient,
    private val json: Json // Keep for potential complex arg serialization if needed
) : ApiService {

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
        webSocketClient.sendCommand("players/cmd/play_pause", jsonArgs("player_id" to playerId))
    }

    override suspend fun playerCommandStop(playerId: String) {
        webSocketClient.sendCommand("players/cmd/stop", jsonArgs("player_id" to playerId))
    }

    override suspend fun playerCommandNext(playerId: String) {
        webSocketClient.sendCommand("players/cmd/next", jsonArgs("player_id" to playerId))
    }

    override suspend fun playerCommandPrevious(playerId: String) {
        webSocketClient.sendCommand("players/cmd/previous", jsonArgs("player_id" to playerId))
    }

    override suspend fun playerCommandSeek(playerId: String, positionSeconds: Int) {
        webSocketClient.sendCommand("players/cmd/seek", jsonArgs("player_id" to playerId, "position" to positionSeconds))
    }

    override suspend fun playerCommandPowerToggle(playerId: String) {
        println("ApiServiceImpl: WARN - playerCommandPowerToggle needs current player state or a dedicated server toggle command. Assuming POWER ON for now.")
        webSocketClient.sendCommand("players/cmd/power", jsonArgs("player_id" to playerId, "powered" to true))
    }

    override suspend fun playerCommandVolumeSet(playerId: String, volumeLevel: Int) {
        webSocketClient.sendCommand("players/cmd/volume_set", jsonArgs("player_id" to playerId, "volume_level" to volumeLevel))
    }

    override suspend fun queueCommandShuffle(queueId: String, shuffleEnabled: Boolean) {
        webSocketClient.sendCommand("player_queues/shuffle", jsonArgs("queue_id" to queueId, "shuffle_enabled" to shuffleEnabled))
    }

    override suspend fun queueCommandRepeat(queueId: String, repeatMode: RepeatMode) {
        webSocketClient.sendCommand("player_queues/repeat", jsonArgs("queue_id" to queueId, "repeat_mode" to repeatMode.name.lowercase()))
    }

    override suspend fun queueCommandPlayIndex(queueId: String, index: Int) {
        webSocketClient.sendCommand("player_queues/play_index", jsonArgs("queue_id" to queueId, "index" to index))
    }

    override suspend fun queueCommandClear(queueId: String) {
        webSocketClient.sendCommand("player_queues/clear", jsonArgs("queue_id" to queueId))
    }

    override suspend fun playMedia(
        queueId: String,
        media: List<String>,
        option: QueueOption?,
        radioMode: Boolean?,
        startItemId: String?
    ) {
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
        webSocketClient.sendCommand("music/tracks/library_items", jsonArgs(
            "favorite" to favorite,
            "search" to search,
            "limit" to limit,
            "offset" to offset,
            "order_by" to orderBy
        ))
        println("ApiServiceImpl: WARN - getLibraryTracks called, but response handling from WebSocket not yet implemented.")
        return emptyList()
    }

    override suspend fun getAllPlayers(): List<Player> {
        webSocketClient.sendCommand("players/all", null) // No args for this command
        println("ApiServiceImpl: INFO - getAllPlayers command sent. Player list will be populated by WebSocket events.")
        return emptyList()
    }

    override suspend fun getAllPlayerQueues(): List<PlayerQueue> {
        webSocketClient.sendCommand("player_queues/all", null) // No args for this command
        println("ApiServiceImpl: INFO - getAllPlayerQueues command sent. Queue list will be populated by WebSocket events.")
        return emptyList()
    }

    override suspend fun getQueueItems(queueId: String, limit: Int, offset: Int): List<QueueItem> {
        webSocketClient.sendCommand("player_queues/items", jsonArgs(
            "queue_id" to queueId,
            "limit" to limit,
            "offset" to offset
        ))
        println("ApiServiceImpl: WARN - getQueueItems called, but response handling from WebSocket not yet implemented.")
        return emptyList()
    }
} 