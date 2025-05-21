package com.mass.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.mass.client.core.network.WebSocketClient
import com.mass.client.di.allModules
import com.mass.client.feature_player.ui.NowPlayingScreen
import com.mass.client.feature_player.ui.PlayerSelectionScreen
import com.mass.client.feature_player.viewmodel.PlayerViewModel
import com.mass.client.ui.theme.KmpClientAppTheme
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinApplication(application = {
        modules(allModules)
        // If you have platform-specific Koin initializations (like androidContext()),
        // those are typically done in the platform-specific Application class (e.g., MainApplication.kt for Android)
        // or via expect/actual for common Koin setup functions.
    }) {
        KmpClientAppTheme {
            val webSocketClient: WebSocketClient = koinInject<WebSocketClient>()
            val playerViewModel: PlayerViewModel = koinInject<PlayerViewModel>()
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                val serverBaseUrl = "http://192.168.1.102:8095" // IMPORTANT: Verify this is correct

                if (serverBaseUrl == "http://YOUR_SERVER_IP_OR_HOSTNAME:8095" || serverBaseUrl.isBlank() || serverBaseUrl == "http://192.168.1.102:8095" /* Temp check to remind user */) {
                    println("App.kt: CRITICAL - Please update 'serverBaseUrl' with your Music Assistant server's actual address if 192.168.1.102:8095 is not it.")
                } 
                // Always attempt connection if a non-placeholder URL is set implicitly (even if it's the default one you typed)
                // The critical print is just a reminder.
                if (!(serverBaseUrl == "http://YOUR_SERVER_IP_OR_HOSTNAME:8095" || serverBaseUrl.isBlank())) {
                    coroutineScope.launch {
                        println("App.kt: Attempting to connect WebSocket to: $serverBaseUrl")
                        webSocketClient.connect(serverBaseUrl) 
                    }
                }
            }

            val observedPlayerId by playerViewModel.observedPlayerId.collectAsState()

            if (observedPlayerId == null) {
                PlayerSelectionScreen(viewModel = playerViewModel) {
                    // This callback is called when a player is selected in PlayerSelectionScreen
                    // PlayerViewModel.setActivePlayer would have been called, updating observedPlayerId
                    // This will trigger recomposition, and the `if` condition will change.
                    println("App.kt: Player selected, should navigate to NowPlayingScreen implicitly.")
                }
            } else {
                NowPlayingScreen(viewModel = playerViewModel)
            }
        }
    }
} 