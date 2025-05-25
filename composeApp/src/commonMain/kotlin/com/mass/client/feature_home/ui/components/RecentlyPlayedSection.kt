package com.mass.client.feature_home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.layout.ContentScale // Not directly used in this card's ArtworkImage call if we rely on ArtworkImage's internal scale
import androidx.compose.ui.unit.dp
import com.mass.client.core.model.ItemMapping
// TODO: Add KamelImage for actual image loading
// import io.kamel.image.KamelImage
// import io.kamel.image.asyncPainterResource

// Added import for ArtworkImage
import com.mass.client.feature_player.ui.ArtworkImage
import com.mass.client.feature_home.viewmodel.HomeViewModel

@Composable
fun RecentlyPlayedSection(
    recentlyPlayedItems: List<ItemMapping>,
    onItemClick: (ItemMapping) -> Unit,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Recently Played", // TODO: Use string resource from commonRes
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        if (recentlyPlayedItems.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recentlyPlayedItems, key = { "${it.provider}-${it.item_id}" }) { item ->
                    RecentlyPlayedItemCard(
                        item = item,
                        onCardClick = { onItemClick(item) },
                        onPlayClick = { homeViewModel.onPlayRecentlyPlayedItem(item) },
                        onMenuClick = { /* TODO: Implement menu action */ println("Menu clicked for ${item.name}") }
                    )
                }
            }
        } else {
            Text(
                text = "No recently played items found.", // TODO: Use string resource
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun RecentlyPlayedItemCard(
    item: ItemMapping,
    onCardClick: () -> Unit,
    onPlayClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onCardClick) // Main card click
    ) {
        Column(
            // horizontalAlignment = Alignment.CenterHorizontally // Let children align themselves
        ) {
            Box(
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium), // Clip the box for play button background
                contentAlignment = Alignment.Center
            ) {
                ArtworkImage(
                    url = item.image?.path,
                    modifier = Modifier.fillMaxSize() // Artwork fills the Box
                )
                // Play button overlay
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), shape = MaterialTheme.shapes.extraLarge)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play ${item.name}",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2, // Allow two lines for longer names
                    modifier = Modifier.weight(1f) // Take available space, leave room for icon
                )
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(36.dp) // Smaller icon button
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More options for ${item.name}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Optional: Add subtitle (e.g., artist name if available and applicable)
            // This requires enhancing ItemMapping or having more specific types
            Spacer(modifier = Modifier.height(8.dp)) // Bottom padding
        }
    }
}