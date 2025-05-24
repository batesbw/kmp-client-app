package com.mass.client.core.network

import com.mass.client.core.model.Player
import com.mass.client.core.model.PlayerQueue
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import java.util.concurrent.ConcurrentHashMap

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

    private var connectionReadyDeferred = CompletableDeferred<Unit>()

    private val _events = MutableSharedFlow<ServerEvent>()
    val events: SharedFlow<ServerEvent> = _events.asSharedFlow()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; prettyPrint = true }

    private val pendingCommands = ConcurrentHashMap<Int, CompletableDeferred<JsonElement?>>()

    suspend fun connect(baseUrl: String) {
        if (session?.isActive == true && clientScope.isActive && connectionReadyDeferred.isCompleted && !connectionReadyDeferred.isCancelled) {
            println("WebSocketClient: Already connected and ready.")
            return
        }
        if (connectionReadyDeferred.isCompleted) {
            connectionReadyDeferred = CompletableDeferred()
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
                                        if (!connectionReadyDeferred.isCompleted) {
                                            connectionReadyDeferred.complete(Unit)
                                            println("WebSocketClient: Connection ready, ServerInfo received: $serverInfoMsg")
                                        } else {
                                            println("WebSocketClient: Received subsequent ServerInfo: $serverInfoMsg")
                                        }
                                    }
                                    text.contains("\"event\"") -> {
                                        val eventMsg = json.decodeFromString<ServerEvent>(text)
                                        _events.emit(eventMsg)
                                    }
                                    text.contains("\"result\"") && text.contains("\"message_id\"") -> {
                                        val resultMsg = json.decodeFromString<SuccessResultMessage>(text)
                                        println("WebSocketClient: Received Success Result for ID ${resultMsg.message_id}, Data: ${resultMsg.result}")
                                        pendingCommands.remove(resultMsg.message_id)?.let {
                                            println("WebSocketClient: Completing command ID ${resultMsg.message_id} successfully.")
                                            it.complete(resultMsg.result)
                                        } ?: println("WebSocketClient: No pending command found for success ID ${resultMsg.message_id}")
                                    }
                                    text.contains("\"error_code\"") && text.contains("\"message_id\"") -> {
                                        val errorMsg = json.decodeFromString<ErrorResultMessage>(text)
                                        val exception = Exception("Server error [${errorMsg.error_code}]: ${errorMsg.details}")
                                        println("WebSocketClient: Received Error Result for ID ${errorMsg.message_id}: ${errorMsg.error_code} - ${errorMsg.details}")
                                        pendingCommands.remove(errorMsg.message_id)?.let {
                                            println("WebSocketClient: Completing command ID ${errorMsg.message_id} exceptionally due to server error.")
                                            it.completeExceptionally(exception)
                                        } ?: println("WebSocketClient: No pending command found for error ID ${errorMsg.message_id}")
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
                    println("WebSocketClient: Incoming collector ended (session closed or error). Completing all pending commands exceptionally.")
                    pendingCommands.forEach { (id, deferred) ->
                        if (!deferred.isCompleted) {
                            println("WebSocketClient: Forcefully completing command ID $id exceptionally due to collector ending.")
                            deferred.completeExceptionally(Exception("WebSocket incoming collector ended"))
                        }
                    }
                    pendingCommands.clear()
                    if (!connectionReadyDeferred.isCompleted) {
                        println("WebSocketClient: Collector ended before connection was ready. Completing connectionReadyDeferred exceptionally.")
                        connectionReadyDeferred.completeExceptionally(Exception("WebSocket collector ended before ready"))
                    }
                }
            }
        } catch (e: Exception) {
            println("WebSocketClient: Connection error to $wsUrl: ${e.message}. Completing all pending commands exceptionally.")
            session = null
            pendingCommands.forEach { (id, deferred) ->
                if (!deferred.isCompleted) {
                    println("WebSocketClient: Forcefully completing command ID $id exceptionally due to connection error.")
                    deferred.completeExceptionally(e)
                }
            }
            pendingCommands.clear()
            if (!connectionReadyDeferred.isCompleted) {
                println("WebSocketClient: Connection attempt failed. Completing connectionReadyDeferred exceptionally.")
                connectionReadyDeferred.completeExceptionally(e)
            }
        }
    }

    suspend fun awaitConnectionReady(timeoutMillis: Long = 15000) {
        println("WebSocketClient: Awaiting connection ready...")
        withTimeoutOrNull(timeoutMillis) {
            connectionReadyDeferred.await()
        } ?: run {
            println("WebSocketClient: Timeout waiting for connection to be ready after $timeoutMillis ms.")
            throw Exception("Timeout waiting for WebSocket connection to be ready.")
        }
        println("WebSocketClient: Connection is ready.")
    }

    private suspend fun sendRawCommand(command: String, args: Map<String, JsonElement?>? = null, expectResponse: Boolean = false): Pair<Int, CompletableDeferred<JsonElement?>?> {
        val currentId = ++messageIdCounter
        val cmdMessage = CommandMessage(
            message_id = currentId,
            command = command,
            args = args
        )
        var deferredResponse: CompletableDeferred<JsonElement?>? = null
        if (expectResponse) {
            deferredResponse = CompletableDeferred()
            pendingCommands[currentId] = deferredResponse
            println("WebSocketClient: Stored deferred for command ID $currentId, command: $command")
        }

        try {
            val jsonCommand = json.encodeToString(cmdMessage)
            session?.send(Frame.Text(jsonCommand))
            println("WebSocketClient: Sent command ID $currentId: $command, args: $args")
        } catch (e: Exception) {
            println("WebSocketClient: Error sending command $command (ID $currentId): ${e.message}")
            pendingCommands.remove(currentId)
            deferredResponse?.let {
                if (!it.isCompleted) {
                    println("WebSocketClient: Completing command ID $currentId exceptionally due to send error.")
                    it.completeExceptionally(e)
                }
            }
        }
        return currentId to deferredResponse
    }

    suspend fun sendCommand(command: String, args: Map<String, JsonElement?>? = null) {
        sendRawCommand(command, args, expectResponse = false)
    }

    suspend fun sendCommandAndWaitForResponse(command: String, args: Map<String, JsonElement?>? = null, timeoutMillis: Long = 10000): JsonElement? {
        val (commandId, deferred) = sendRawCommand(command, args, expectResponse = true)
        println("WebSocketClient: sendCommandAndWaitForResponse for command ID $commandId ($command), awaiting deferred.")
        if (deferred == null) {
            println("WebSocketClient: sendCommandAndWaitForResponse for command ID $commandId ($command) - deferred was null before await.")
            throw Exception("Failed to get deferred for command (it was null): $command")
        }
        val result = deferred.let {
            withTimeoutOrNull(timeoutMillis) {
                try {
                    println("WebSocketClient: Awaiting command ID $commandId ($command)...")
                    val awaitedResult = it.await()
                    println("WebSocketClient: Awaited command ID $commandId ($command) completed. Result: $awaitedResult")
                    awaitedResult
                } catch (e: Exception) {
                    println("WebSocketClient: Awaiting command ID $commandId ($command) threw exception: ${e.message}")
                    throw e
                }
            }
        }
        if (result == null && deferred.isActive) {
             println("WebSocketClient: sendCommandAndWaitForResponse for command ID $commandId ($command) timed out after $timeoutMillis ms.")
        } else if (result == null && !deferred.isActive) {
             println("WebSocketClient: sendCommandAndWaitForResponse for command ID $commandId ($command) resulted in null, deferred was completed (possibly with null or exceptionally).")
        }

        return result ?: run {
            val exception = deferred.getCompletionExceptionOrNull()
            if (exception != null) {
                println("WebSocketClient: sendCommandAndWaitForResponse for command ID $commandId ($command) failed with deferred exception: ${exception.message}")
                throw Exception("Command $command (ID $commandId) failed or timed out", exception)
            }
            println("WebSocketClient: sendCommandAndWaitForResponse for command ID $commandId ($command) resulted in null and no exception, assuming timeout or null completion.")
            throw Exception("Command $command (ID $commandId) failed or timed out (result was null).")
        }
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
                println("WebSocketClient: Disconnect called. Completing all pending commands exceptionally.")
                pendingCommands.forEach { (id, deferred) ->
                    if (!deferred.isCompleted) {
                        println("WebSocketClient: Forcefully completing command ID $id exceptionally due to disconnect call.")
                        deferred.completeExceptionally(Exception("WebSocket disconnected by client call"))
                    }
                }
                pendingCommands.clear()
                if (connectionReadyDeferred.isCompleted) {
                    connectionReadyDeferred = CompletableDeferred()
                    println("WebSocketClient: ConnectionReadyDeferred has been reset due to disconnect.")
                } else {
                    connectionReadyDeferred.completeExceptionally(Exception("Disconnected before connection was ready"))
                    println("WebSocketClient: ConnectionReadyDeferred was completed exceptionally as disconnect occurred before ready.")
                }
            }
        }
    }
} 