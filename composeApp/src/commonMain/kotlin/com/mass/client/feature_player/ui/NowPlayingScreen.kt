package com.mass.client.feature_player.ui

// import androidx.compose.foundation.Image // No longer needed for this Composable if only using AsyncImage
import androidx.compose.foundation.background
// import androidx.compose.foundation.clickable // Not used in ArtworkImage
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* 
import androidx.compose.material3.* 
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
// import androidx.compose.ui.graphics.Color // Not directly used here
// import androidx.compose.ui.graphics.painter.ColorPainter // Not directly used here
import androidx.compose.ui.layout.ContentScale
// import androidx.compose.ui.text.style.TextAlign // Not used in ArtworkImage
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mass.client.core.model.PlayerState
import com.mass.client.core.model.RepeatMode
import com.mass.client.feature_player.viewmodel.PlayerViewModel
import com.mass.client.ui.theme.KmpClientAppTheme 
import org.koin.compose.koinInject 

import coil3.compose.AsyncImage // Import for Coil

@Composable
fun ArtworkImage(url: String?, modifier: Modifier = Modifier) {
    if (url != null && url.isNotBlank()) {
        AsyncImage(
            model = url,
            contentDescription = "Album Artwork",
            modifier = modifier,
            contentScale = ContentScale.Crop,
            // Optional: Add placeholder and error drawables/painters if you have them
            // placeholder = painterResource(R.drawable.placeholder), // Example for Android
            // error = painterResource(R.drawable.error_placeholder)
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.MusicNote,
                contentDescription = "Album Artwork Placeholder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp) // Give placeholder icon a decent size
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    viewModel: PlayerViewModel = koinInject() // Get ViewModel via Koin
) {
    val currentTrackName by viewModel.currentTrackName.collectAsState()
    val currentTrackArtist by viewModel.currentTrackArtist.collectAsState()
    val artworkUrl by viewModel.currentTrackArtworkUrl.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val volumeLevel by viewModel.volumeLevel.collectAsState()
    val shuffleEnabled by viewModel.shuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    // val playerState by viewModel.playerState.collectAsState() // Not directly used in UI yet

    LaunchedEffect(Unit) {
        if (viewModel.observedPlayerId == null) {
            println("NowPlayingScreen: observedPlayerId is null. Please set it in PlayerViewModel or via a player selection UI. For testing, try viewModel.setActivePlayer(\"your_player_id\") in your app's entry point after WebSocket connect.")
        }
    }

    KmpClientAppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Now Playing") },
                    // TODO: Add navigation icon (e.g., back arrow)
                )
            }
        ) {
            paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround // Distributes space a bit more
            ) {

                ArtworkImage(
                    url = artworkUrl,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant) // Background for the area
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentTrackName,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentTrackArtist,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Slider(
                        value = progress,
                        onValueChange = { newProgress -> viewModel.seekToFraction(newProgress) },
                        modifier = Modifier.fillMaxWidth(),
                        // enabled = playerState != PlayerState.IDLE // Consider enabling/disabling based on state
                    )
                    // TODO: Add current time / total time labels below slider
                }

                Spacer(modifier = Modifier.height(8.dp)) // Reduced space before controls

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        Icon(
                            Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.previousTrack() }) {
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(36.dp))
                    }
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.PauseCircleFilled else Icons.Filled.PlayCircleFilled,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(72.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.nextTrack() }) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(36.dp))
                    }
                    IconButton(onClick = { viewModel.cycleRepeatMode() }) {
                        val repeatIcon = when (repeatMode) {
                            RepeatMode.OFF -> Icons.Filled.Repeat
                            RepeatMode.ONE -> Icons.Filled.RepeatOne
                            RepeatMode.ALL -> Icons.Filled.RepeatOn // Assuming you have RepeatOn or similar
                        }
                        Icon(
                            repeatIcon,
                            contentDescription = "Repeat Mode",
                            tint = if (repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Icon(Icons.Filled.VolumeDown, contentDescription = "Volume Down")
                    Slider(
                        value = volumeLevel.toFloat() / 100f, // Assuming volume is 0-100
                        onValueChange = { newVolumeFraction -> viewModel.setVolume((newVolumeFraction * 100).toInt()) },
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        // enabled = playerState != PlayerState.IDLE
                    )
                    Icon(Icons.Filled.VolumeUp, contentDescription = "Volume Up")
                }
                 Spacer(modifier = Modifier.weight(1f)) // Pushes controls up a bit if screen is tall
            }
        }
    }
} 