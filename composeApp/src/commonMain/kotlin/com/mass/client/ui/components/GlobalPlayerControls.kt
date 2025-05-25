package com.mass.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mass.client.core.model.PlayerState
import com.mass.client.feature_player.viewmodel.PlayerViewModel
import org.koin.compose.koinInject

/**
 * Global player controls component that displays a mini player at the bottom of the screen.
 * This replicates the functionality of the frontend's Footer/Player.vue component.
 * 
 * Features:
 * - Track artwork, title, and artist display
 * - Play/pause button with proper state management
 * - Progress bar with seek functionality
 * - Next/previous track buttons (on larger screens)
 * - Click to expand to full Now Playing screen
 * - Real-time state synchronization via WebSocket
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalPlayerControls(
    onExpandToFullPlayer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = koinInject()
) {
    // Collect player state from ViewModel
    val activePlayer by viewModel.activePlayer.collectAsState()
    val currentTrackName by viewModel.currentTrackName.collectAsState()
    val currentTrackArtist by viewModel.currentTrackArtist.collectAsState()
    val artworkUrl by viewModel.currentTrackArtworkUrl.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val availablePlayers by viewModel.availablePlayers.collectAsState()

    // Show the mini player if there are available players OR if there's an active player
    // This ensures the global player is ALWAYS visible when players are available
    if (availablePlayers.isNotEmpty() || activePlayer != null) {
        // Debug logging to help troubleshoot visibility issues
        println("GlobalPlayerControls: Showing mini player - availablePlayers.size=${availablePlayers.size}, activePlayer=${activePlayer?.display_name}")
        
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                // Progress bar at the top (thin line with seek functionality)
                SeekableProgressBar(
                    progress = progress,
                    onSeek = { newProgress -> viewModel.seekToFraction(newProgress) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )

                // Main content row
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onExpandToFullPlayer() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album artwork
                    AlbumArtworkMini(
                        url = artworkUrl,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Track info (title and artist)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentTrackName.isNotBlank()) currentTrackName else "No track playing",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (currentTrackName.isNotBlank()) 
                                MaterialTheme.colorScheme.onSurface 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (currentTrackArtist.isNotBlank() && currentTrackArtist != "-") {
                            Text(
                                text = currentTrackArtist,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (activePlayer != null) {
                            Text(
                                text = "Ready to play",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Control buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Previous button (only on larger screens)
                        // TODO: Add breakpoint logic similar to frontend
                        IconButton(
                            onClick = { viewModel.previousTrack() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.SkipPrevious,
                                contentDescription = "Previous",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Play/Pause button
                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Next button (only on larger screens)
                        // TODO: Add breakpoint logic similar to frontend
                        IconButton(
                            onClick = { viewModel.nextTrack() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.SkipNext,
                                contentDescription = "Next",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Debug logging when not showing the mini player
        println("GlobalPlayerControls: NOT showing mini player - availablePlayers.size=${availablePlayers.size}, activePlayer=${activePlayer?.display_name}")
    }
}

/**
 * Seekable progress bar component that allows users to tap to seek to a specific position.
 * Replicates the functionality of the frontend's PlayerTimeline.vue component.
 */
@Composable
private fun SeekableProgressBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Calculate the progress based on tap position
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek(newProgress)
                }
            }
    ) {
        // Background track
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        
        // Progress indicator
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

/**
 * Mini album artwork component for the global player controls.
 * Shows a placeholder icon when no artwork is available.
 */
@Composable
private fun AlbumArtworkMini(
    url: String?,
    modifier: Modifier = Modifier
) {
    if (url != null && url.isNotBlank()) {
        AsyncImage(
            model = url,
            contentDescription = "Album Artwork",
            modifier = modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.MusicNote,
                contentDescription = "Album Artwork Placeholder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
} 