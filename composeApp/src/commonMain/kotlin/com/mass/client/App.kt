package com.mass.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.mass.client.core.network.WebSocketClient
// import com.mass.client.di.allModules // No longer needed here
import com.mass.client.feature_player.ui.NowPlayingScreen
import com.mass.client.navigation.HomeTab
import com.mass.client.navigation.LibraryTab
import com.mass.client.navigation.SearchTab
import com.mass.client.ui.components.GlobalPlayerControls
import com.mass.client.ui.theme.KmpClientAppTheme
import com.mass.client.ui.theme.DarkPrimary // For selected item color
import kotlinx.coroutines.launch
// import org.koin.compose.KoinApplication // No longer needed
import org.koin.compose.koinInject

// TODO: Move this to its own file e.g. com.mass.client.ui.components.BottomPlayerControls.kt
@Composable
fun BottomPlayerControlsPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp) // Approximate height from screenshot
            // .background(MaterialTheme.colorScheme.surfaceVariant) // Example background
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Global Bottom Player Controls (TODO)", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun App() {
    // KoinApplication wrapper removed. Koin is initialized globally in MyApplication.
    KmpClientAppTheme {
        val webSocketClient: WebSocketClient = koinInject<WebSocketClient>()
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            val serverBaseUrl = "http://192.168.1.102:8095" // IMPORTANT: Verify this is correct

            if (serverBaseUrl == "http://YOUR_SERVER_IP_OR_HOSTNAME:8095" || serverBaseUrl.isBlank() || serverBaseUrl == "http://192.168.1.102:8095" /* Temp check to remind user */) {
                println("App.kt: CRITICAL - Please update 'serverBaseUrl' with your Music Assistant server's actual address if 192.168.1.102:8095 is not it.")
            }
            if (!(serverBaseUrl == "http://YOUR_SERVER_IP_OR_HOSTNAME:8095" || serverBaseUrl.isBlank())) {
                coroutineScope.launch {
                    println("App.kt: Attempting to connect WebSocket to: $serverBaseUrl")
                    webSocketClient.connect(serverBaseUrl)
                }
            }
        }

        // Main navigator for full-screen navigation (like Now Playing)
        Navigator(HomeTab) { navigator ->
            TabNavigator(HomeTab) { tabNavigator ->
                Scaffold(
                    content = { paddingValues -> // These paddingValues now account for top bars AND bottom controls
                        CurrentTab() // Screens will use these paddingValues
                    },
                    bottomBar = {
                        Column {
                            // Global player controls (mini player)
                            GlobalPlayerControls(
                                onExpandToFullPlayer = {
                                    // Navigate to full Now Playing screen
                                    navigator.push(NowPlayingScreen())
                                }
                            )
                            
                            // Tab navigation
                            NavigationBar {
                                TabNavigationItem(HomeTab)
                                TabNavigationItem(SearchTab)
                                TabNavigationItem(LibraryTab)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == tab

    NavigationBarItem(
        selected = isSelected,
        onClick = { tabNavigator.current = tab },
        icon = {
            tab.options.icon?.let { painter ->
                Icon(
                    painter = painter,
                    contentDescription = tab.options.title
                )
            }
        },
        label = { Text(tab.options.title) },
        alwaysShowLabel = true, // Or false based on your preference
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = DarkPrimary, // Use a color from your theme
            selectedTextColor = DarkPrimary,
            // indicatorColor = MaterialTheme.colorScheme.primaryContainer // Optional indicator
            // unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            // unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
} 