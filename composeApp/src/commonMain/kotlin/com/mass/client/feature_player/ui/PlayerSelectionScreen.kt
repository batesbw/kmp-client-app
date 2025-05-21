package com.mass.client.feature_player.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mass.client.core.model.Player
import com.mass.client.feature_player.viewmodel.PlayerViewModel
import com.mass.client.ui.theme.KmpClientAppTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSelectionScreen(
    viewModel: PlayerViewModel = koinInject(),
    onPlayerSelected: () -> Unit // Callback to notify when a player is selected
) {
    val availablePlayers by viewModel.availablePlayers.collectAsState()

    KmpClientAppTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Select a Player") })
            }
        ) {
            paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (availablePlayers.isEmpty()) {
                    Text("No players available. Make sure players are active on your Music Assistant server.")
                    // TODO: Add a refresh button or auto-retry logic here later
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(availablePlayers, key = { it.player_id }) { player ->
                            PlayerListItem(player = player) {
                                viewModel.setActivePlayer(player.player_id)
                                onPlayerSelected() // Notify that selection is done
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerListItem(
    player: Player,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(player.display_name) },
        supportingContent = { Text(player.provider) }, // Or player.state.name.lowercase() for state
        leadingContent = {
            Icon(
                Icons.Filled.Speaker, // Or use player.icon if available and mapped to Compose icons
                contentDescription = "Player"
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    )
} 