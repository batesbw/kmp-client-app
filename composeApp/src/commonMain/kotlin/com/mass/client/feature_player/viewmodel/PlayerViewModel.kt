package com.mass.client.feature_player.viewmodel

import com.mass.client.core.model.Player
import com.mass.client.core.model.PlayerQueue
import com.mass.client.core.model.QueueItem
import com.mass.client.core.model.PlayerState
import com.mass.client.core.model.RepeatMode
import com.mass.client.core.model.Track
import com.mass.client.core.model.PlayableTrack
import com.mass.client.core.model.QueueOption
import com.mass.client.core.network.ApiService
import com.mass.client.core.network.WebSocketClient
import com.mass.client.core.network.ServerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

open class BaseViewModelProtected {
    protected val viewModelScope = CoroutineScope(Dispatchers.Main + Job())
    // TODO: Implement a proper way to cancel the Job when the ViewModel is cleared.
}

class PlayerViewModel(
    private val apiService: ApiService,
    private val webSocketClient: WebSocketClient,
    private val json: Json
) : BaseViewModelProtected() {

    private val _availablePlayers = MutableStateFlow<List<Player>>(emptyList())
    val availablePlayers: StateFlow<List<Player>> = _availablePlayers.asStateFlow()

    private val _activePlayer = MutableStateFlow<Player?>(null)
    val activePlayer: StateFlow<Player?> = _activePlayer.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _currentTrackName = MutableStateFlow("Nothing Playing")
    val currentTrackName: StateFlow<String> = _currentTrackName.asStateFlow()

    private val _currentTrackArtist = MutableStateFlow("")
    val currentTrackArtist: StateFlow<String> = _currentTrackArtist.asStateFlow()

    private val _currentTrackArtworkUrl = MutableStateFlow<String?>(null)
    val currentTrackArtworkUrl: StateFlow<String?> = _currentTrackArtworkUrl.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _volumeLevel = MutableStateFlow(50)
    val volumeLevel: StateFlow<Int> = _volumeLevel.asStateFlow()

    private val _activeQueue = MutableStateFlow<PlayerQueue?>(null)
    val activeQueue: StateFlow<PlayerQueue?> = _activeQueue.asStateFlow()

    private val _currentQueueItems = MutableStateFlow<List<QueueItem>>(emptyList())
    val currentQueueItems: StateFlow<List<QueueItem>> = _currentQueueItems.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _observedPlayerId = MutableStateFlow<String?>(null)
    val observedPlayerId: StateFlow<String?> = _observedPlayerId.asStateFlow()

    init {
        webSocketClient.events
            .onEach { event -> handleServerEvent(event) }
            .launchIn(viewModelScope)

        loadAvailablePlayers()
    }

    private fun loadAvailablePlayers() {
        viewModelScope.launch {
            try {
                _availablePlayers.value = apiService.getAllPlayers()
                if (_availablePlayers.value.isEmpty()) {
                    println("PlayerViewModel: getAllPlayers returned empty. Waiting for WebSocket events or check server.")
                }
            } catch (e: Exception) {
                println("PlayerViewModel: Error fetching all players: ${e.message}")
            }
        }
    }

    private fun handleServerEvent(serverEvent: ServerEvent) {
        when (serverEvent.event) {
            "PLAYER_UPDATED" -> {
                serverEvent.data?.let {
                    try {
                        val player = json.decodeFromJsonElement<Player>(it)
                        val currentList = _availablePlayers.value.toMutableList()
                        val existingPlayerIndex = currentList.indexOfFirst { p -> p.player_id == player.player_id }
                        if (existingPlayerIndex != -1) {
                            currentList[existingPlayerIndex] = player
                        } else {
                            currentList.add(player)
                        }
                        _availablePlayers.value = currentList
                        
                        if (player.player_id == _observedPlayerId.value) {
                            _activePlayer.value = player
                            updateUiFromPlayer(player)
                        }
                    } catch (e: Exception) {
                        println("PlayerViewModel: Error parsing PLAYER_UPDATED data: ${e.message} from data: $it")
                    }
                }
            }
            "PLAYER_REMOVED" -> {
                serverEvent.object_id?.let { playerIdToRemove ->
                    _availablePlayers.value = _availablePlayers.value.filterNot { p -> p.player_id == playerIdToRemove }
                    if (playerIdToRemove == _observedPlayerId.value) {
                        clearActivePlayerData()
                        _observedPlayerId.value = null
                    }
                }
            }
            "QUEUE_UPDATED" -> {
                serverEvent.data?.let {
                    try {
                        val queue = json.decodeFromJsonElement<PlayerQueue>(it)
                        if (queue.queue_id == _observedPlayerId.value) {
                            _activeQueue.value = queue
                            updateUiFromQueue(queue)
                        }
                    } catch (e: Exception) {
                        println("PlayerViewModel: Error parsing QUEUE_UPDATED data: ${e.message} from data: $it")
                    }
                }
            }
            "QUEUE_ITEMS_UPDATED" -> {
                serverEvent.data?.let {
                    try {
                        val qId = serverEvent.object_id
                        if (qId == _observedPlayerId.value) {
                            println("PlayerViewModel: QUEUE_ITEMS_UPDATED received for $qId, reloading items.")
                            loadQueueItems()
                        }
                    } catch (e: Exception) {
                        println("PlayerViewModel: Error parsing QUEUE_ITEMS_UPDATED data: ${e.message} from data: $it")
                    }
                }
            }
            else -> Unit
        }
    }

    private fun clearActivePlayerData() {
        _activePlayer.value = null
        _activeQueue.value = null
        _currentTrackName.value = "Nothing Playing"
        _currentTrackArtist.value = ""
        _currentTrackArtworkUrl.value = null
        _isPlaying.value = false
        _progress.value = 0f
        _playerState.value = PlayerState.IDLE
        _currentQueueItems.value = emptyList()
    }

    private fun updateUiFromPlayer(player: Player) {
        _playerState.value = player.state ?: PlayerState.IDLE
        _isPlaying.value = player.state == PlayerState.PLAYING
        _currentTrackName.value = player.current_media?.title ?: "-"
        _currentTrackArtist.value = player.current_media?.artist ?: "-"
        _currentTrackArtworkUrl.value = player.current_media?.image_url
        _volumeLevel.value = player.volume_level ?: 50

        player.elapsed_time?.let { elapsed ->
            player.current_media?.duration?.let { duration ->
                if (duration > 0) {
                    _progress.value = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                } else {
                    _progress.value = 0f
                }
            } ?: run { _progress.value = 0f }
        } ?: run { _progress.value = 0f }
    }

    private fun updateUiFromQueue(queue: PlayerQueue) {
        _shuffleEnabled.value = queue.shuffle_enabled
        _repeatMode.value = queue.repeat_mode
        queue.current_item?.let {
            _currentTrackName.value = it.media_item?.name ?: it.name
            val artists = (it.media_item as? PlayableTrack)?.track?.artists?.joinToString { artist -> artist.name } ?: ""
            _currentTrackArtist.value = artists
            _currentTrackArtworkUrl.value = it.media_item?.metadata?.images?.firstOrNull()?.path ?: it.image_url
        }
        if (queue.queue_id == _observedPlayerId.value) {
             _playerState.value = queue.state
            _isPlaying.value = queue.state == PlayerState.PLAYING
            queue.elapsed_time.let { elapsed ->
                queue.current_item?.duration?.let { duration ->
                    if (duration > 0) {
                         _progress.value = (elapsed / duration).coerceIn(0f, 1f)
                    } else {
                        _progress.value = 0f
                    }
                } ?: run { _progress.value = 0f }
            } ?: run { _progress.value = 0f }
        }
    }

    fun setActivePlayer(playerId: String) {
        if (_observedPlayerId.value == playerId && _activePlayer.value?.player_id == playerId) {
            if (_currentQueueItems.value.isEmpty() && _activeQueue.value != null) loadQueueItems()
            return
        }
        println("PlayerViewModel: Setting active player to $playerId")
        clearActivePlayerData()
        _observedPlayerId.value = playerId

        _availablePlayers.value.find { it.player_id == playerId }?.let {
            _activePlayer.value = it
            updateUiFromPlayer(it)
        }
        viewModelScope.launch {
            try {
                val queue = apiService.getAllPlayerQueues().find { it.queue_id == playerId }
                if (queue != null) {
                    _activeQueue.value = queue
                    updateUiFromQueue(queue)
                    loadQueueItems()
                } else {
                    println("PlayerViewModel: No specific queue found for $playerId initially, relying on events.")
                }
            } catch (e: Exception) {
                println("PlayerViewModel: Error fetching queue for $playerId: ${e.message}")
            }
        }
    }

    fun togglePlayPause() {
        _observedPlayerId.value?.let {
            viewModelScope.launch { apiService.playerCommandPlayPause(it) }
        }
    }

    fun nextTrack() {
        _observedPlayerId.value?.let {
            viewModelScope.launch { apiService.playerCommandNext(it) }
        }
    }

    fun previousTrack() {
        _observedPlayerId.value?.let {
            viewModelScope.launch { apiService.playerCommandPrevious(it) }
        }
    }

    fun seekToFraction(fraction: Float) {
        _observedPlayerId.value?.let { pId ->
            val currentItemDuration = _activeQueue.value?.current_item?.duration ?: _activePlayer.value?.current_media?.duration
            currentItemDuration?.let {
                val targetSeconds = (it * fraction).toInt()
                viewModelScope.launch { apiService.playerCommandSeek(pId, targetSeconds) }
            }
        }
    }

    fun setVolume(volume: Int) {
        _observedPlayerId.value?.let { pId ->
            val clampedVolume = volume.coerceIn(0, 100)
            viewModelScope.launch {
                apiService.playerCommandVolumeSet(pId, clampedVolume)
            }
        }
    }

    fun toggleShuffle() {
        _observedPlayerId.value?.let { qId -> 
            viewModelScope.launch {
                apiService.queueCommandShuffle(qId, !_shuffleEnabled.value)
            }
        }
    }

    fun cycleRepeatMode() {
        _observedPlayerId.value?.let { qId ->
            val nextMode = when (_repeatMode.value) {
                RepeatMode.OFF -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.OFF
            }
            viewModelScope.launch {
                apiService.queueCommandRepeat(qId, nextMode)
            }
        }
    }

    fun playMediaItems(uris: List<String>, queueOption: QueueOption = QueueOption.REPLACE) {
        _observedPlayerId.value?.let { qId ->
            viewModelScope.launch {
                apiService.playMedia(queueId = qId, media = uris, option = queueOption)
            }
        }
    }

    fun loadQueueItems() {
        _observedPlayerId.value?.let { qId ->
            viewModelScope.launch {
                try {
                    _currentQueueItems.value = apiService.getQueueItems(qId)
                     if (_currentQueueItems.value.isEmpty()){
                        println("PlayerViewModel: getQueueItems returned empty for $qId. Waiting for WebSocket event or check response handling.")
                    }
                } catch (e: Exception) {
                    println("PlayerViewModel: Error fetching queue items for $qId: ${e.message}")
                }
            }
        }
    }
} 