package com.mass.client.core.network

import com.mass.client.core.model.Player
import com.mass.client.core.model.PlayerQueue
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement

@Serializable
data class CommandMessage(
    val message_id: Int? = null,
    val command: String,
    val args: Map<String, JsonElement?>? = null
)

@Serializable
data class ServerEvent(
    val event: String,
    val object_id: String? = null,
    val data: JsonElement? = null
)

@Serializable
data class ServerInfoMessage(
    val server_id: String,
    val server_version: String,
    val schema_version: Int,
    val base_url: String,
    val onboard_done: Boolean
)

@Serializable
data class SuccessResultMessage(
    val message_id: Int,
    val result: JsonElement?
)

@Serializable
data class ErrorResultMessage(
    val message_id: Int,
    val error_code: String,
    val details: String? = null
)

class WebSocketClient(private val httpClient: HttpClient) {

    private var session: DefaultClientWebSocketSession? = null
    private val clientScope = CoroutineScope(Dispatchers.Default + Job())
    private var messageIdCounter = 0

    private val _serverInfo = MutableSharedFlow<ServerInfoMessage>(replay = 1)
    val serverInfo: SharedFlow<ServerInfoMessage> = _serverInfo.asSharedFlow()

    private val _events = MutableSharedFlow<ServerEvent>()
    val events: SharedFlow<ServerEvent> = _events.asSharedFlow()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; prettyPrint = true }

    suspend fun connect(baseUrl: String) {
        if (session?.isActive == true && clientScope.isActive) {
            println("WebSocketClient: Already connected or connecting.")
            return
        }
        val wsUrl = baseUrl.replaceFirst("http", "ws") + "/ws"
        try {
            println("WebSocketClient: Attempting to connect to $wsUrl")
            session = httpClient.webSocketSession(wsUrl)
            println("WebSocketClient: Connection established to $wsUrl")

            clientScope.launch {
                try {
                    session?.incoming?.receiveAsFlow()?.collect { frame ->
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            try {
                                when {
                                    text.contains("\"server_version\"") && text.contains("\"server_id\"") -> {
                                        val serverInfoMsg = json.decodeFromString<ServerInfoMessage>(text)
                                        _serverInfo.emit(serverInfoMsg)
                                        println("WebSocketClient: Received ServerInfo: $serverInfoMsg")
                                    }
                                    text.contains("\"event\"") -> {
                                        val eventMsg = json.decodeFromString<ServerEvent>(text)
                                        _events.emit(eventMsg)
                                    }
                                    text.contains("\"result\"") && text.contains("\"message_id\"") -> {
                                        val resultMsg = json.decodeFromString<SuccessResultMessage>(text)
                                        println("WebSocketClient: Received Success Result for ID ${resultMsg.message_id}")
                                    }
                                    text.contains("\"error_code\"") && text.contains("\"message_id\"") -> {
                                        val errorMsg = json.decodeFromString<ErrorResultMessage>(text)
                                        println("WebSocketClient: Received Error Result for ID ${errorMsg.message_id}: ${errorMsg.error_code} - ${errorMsg.details}")
                                    }
                                    else -> {
                                        println("WebSocketClient: Received unknown message type: $text")
                                    }
                                }
                            } catch (e: Exception) {
                                println("WebSocketClient: Error parsing message \"$text\": ${e.message}")
                            }
                        }
                    }
                } finally {
                    println("WebSocketClient: Incoming collector ended (session closed or error).")
                }
            }
        } catch (e: Exception) {
            println("WebSocketClient: Connection error to $wsUrl: ${e.message}")
            session = null
        }
    }

    suspend fun sendCommand(command: String, args: Map<String, JsonElement?>? = null): Int {
        val currentId = ++messageIdCounter
        val cmdMessage = CommandMessage(
            message_id = currentId,
            command = command,
            args = args
        )
        try {
            val jsonCommand = json.encodeToString(cmdMessage)
            session?.send(Frame.Text(jsonCommand))
            println("WebSocketClient: Sent command ID $currentId: $command, args: $args")
        } catch (e: Exception) {
            println("WebSocketClient: Error sending command $command: ${e.message}")
        }
        return currentId
    }

    fun disconnect() {
        clientScope.launch {
            try {
                session?.close()
                println("WebSocketClient: Disconnected.")
            } catch (e: Exception) {
                println("WebSocketClient: Error during disconnect: ${e.message}")
            } finally {
                session = null
            }
        }
    }
} 