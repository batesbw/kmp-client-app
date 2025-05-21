package com.mass.client.core.network

import com.mass.client.core.model.Player
import com.mass.client.core.model.PlayerQueue
import com.mass.client.core.model.QueueItem
import com.mass.client.core.model.QueueOption
import com.mass.client.core.model.RepeatMode
import com.mass.client.core.model.Track
import com.mass.client.core.model.MediaItem // For playMedia item type

/**
 * Interface for all Music Assistant API calls.
 */
interface ApiService {

    // Player Commands
    suspend fun playerCommandPlayPause(playerId: String)
    suspend fun playerCommandStop(playerId: String) // Added for completeness
    suspend fun playerCommandNext(playerId: String)
    suspend fun playerCommandPrevious(playerId: String)
    suspend fun playerCommandSeek(playerId: String, positionSeconds: Int)
    suspend fun playerCommandPowerToggle(playerId: String)
    suspend fun playerCommandVolumeSet(playerId: String, volumeLevel: Int)
    // Add other player commands as needed: power on/off, volume up/down, mute, group commands

    // Queue Commands
    suspend fun queueCommandShuffle(queueId: String, shuffleEnabled: Boolean)
    suspend fun queueCommandRepeat(queueId: String, repeatMode: RepeatMode)
    suspend fun queueCommandPlayIndex(queueId: String, index: Int) // Simplified, can add item_id variant
    suspend fun queueCommandClear(queueId: String)
    // Add other queue commands: move_item, delete_item etc.

    /**
     * Plays media item(s) on the given queue.
     * @param queueId The ID of the queue.
     * @param media URIs or MediaItem objects to play.
     * @param option How to enqueue the media.
     * @param radioMode Enable radio mode for the given item(s).
     * @param startItemId Optional item ID within a playlist or album to start from.
     */
    suspend fun playMedia(
        queueId: String,
        media: List<String>, // Simplified to list of URIs for now
        option: QueueOption? = null,
        radioMode: Boolean? = null,
        startItemId: String? = null
    )

    // Library Fetching (simplified for now)
    suspend fun getLibraryTracks(
        favorite: Boolean? = null,
        search: String? = null,
        limit: Int? = null,
        offset: Int? = null,
        orderBy: String? = null
    ): List<Track>

    // State Fetching (initial load - though WS events are primary for updates)
    suspend fun getAllPlayers(): List<Player>
    suspend fun getAllPlayerQueues(): List<PlayerQueue>
    suspend fun getQueueItems(queueId: String, limit: Int = 50, offset: Int = 0): List<QueueItem>

    // WebSocket related methods might be exposed via a different mechanism
    // or implicitly handled by the implementation that updates reactive state holders.
} 