package com.mass.client.di

import com.mass.client.core.network.ApiService
import com.mass.client.core.network.ApiServiceImpl
import com.mass.client.core.network.WebSocketClient
import com.mass.client.feature_player.viewmodel.PlayerViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

// Define your Koin modules here
// We'll add actual dependencies as we create them (e.g., ViewModels, Repositories, API services)

val appModule = module {
    // kotlinx.serialization Json instance
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true // Good for debugging, consider turning off for release
            encodeDefaults = true
        }
    }

    // Define a common HttpClient, platforms can provide their own engines if needed
    // by overriding this definition in platform-specific Koin modules.
    single<HttpClient> {
        HttpClient(CIO) { // Using CIO as a default common engine (works on JVM/Desktop)
            install(WebSockets)
            install(Logging) {
                level = LogLevel.HEADERS 
                logger = Logger.SIMPLE // Changed from DEFAULT to SIMPLE
            }
            install(ContentNegotiation) {
                json(get()) // Use the Json instance defined above for Ktor
            }
            // Default request configurations can be added here if needed
            // e.g., defaultRequest { url("your_base_api_url_if_static") }
        }
    }

    // WebSocketClient
    single<WebSocketClient> {
        WebSocketClient(get()) // Injects HttpClient
    }

    // ApiService implementation
    single<ApiService> {
        ApiServiceImpl(get(), get()) // Injects WebSocketClient and Json
    }

    // ViewModels
    // For KMP ViewModels not tied to Android Lifecycle, use factory or single
    // If using a KMM ViewModel library that integrates with Koin, follow its pattern.
    // The koin-compose-viewmodel might require Android specific setup or expect androidx.lifecycle.ViewModel.
    // For a simple KoinComponent based ViewModel like ours:
    factory { PlayerViewModel(get(), get(), get()) }

    // TODO: Add Repositories as they are created
}

// List of all modules
val allModules = listOf(appModule) 