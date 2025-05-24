package com.mass.client.feature_home.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mass.client.feature_home.model.UiPlayer // Make sure this import is correct
import com.mass.client.feature_home.viewmodel.HomeViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayersSection(homeViewModel: HomeViewModel) {
    val players by homeViewModel.uiPlayers.collectAsState()

    if (players.isEmpty()) {
        // Optional: Show a loading state or an empty state message
        // Text("No players available or loading...")
        return
    }

    val pagerState = rememberPagerState(pageCount = { players.size })

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SpeakerGroup,
                contentDescription = "Players",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Players",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp) // To show glimpses of next/prev items
        ) { page ->
            val player = players[page]
            // Apply a modifier to each page for spacing if needed, e.g., .padding(horizontal = 8.dp)
            // However, the HorizontalPager's contentPadding might be sufficient.
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) { // Added small horizontal padding between cards
                PlayerCard(player = player, onPauseClicked = { homeViewModel.onPlayerPauseClicked(player.id) })
            }
        }
        // Optional: Add pager indicators if desired
    }
}

@Composable
fun PlayerCard(player: UiPlayer, onPauseClicked: () -> Unit) {
    if (player.state == com.mass.client.feature_home.model.UiPlayerState.PLAYING || player.state == com.mass.client.feature_home.model.UiPlayerState.PAUSED) {
        ActivePlayerCard(
            player = player,
            onPauseClicked = onPauseClicked
        )
    } else {
        InactivePlayerItem(player = player)
    }
} 