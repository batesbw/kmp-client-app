package com.mass.client.feature_home.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.mass.client.core.model.ItemMapping
// TODO: Add KamelImage for actual image loading
// import io.kamel.image.KamelImage
// import io.kamel.image.asyncPainterResource

@Composable
fun RecentlyPlayedSection(
    recentlyPlayedItems: List<ItemMapping>,
    onItemClick: (ItemMapping) -> Unit,
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
                    RecentlyPlayedItemCard(item = item, onClick = { onItemClick(item) })
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder for AsyncImage - KamelImage would be used here.
            Box(
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // TODO: Replace with KamelImage once dependency is added and configured
                // KamelImage(
                //    resource = asyncPainterResource(data = item.image?.path ?: ""),
                //    contentDescription = item.name,
                //    contentScale = ContentScale.Crop,
                //    modifier = Modifier.fillMaxSize(),
                //    onLoading = { Text("Loading...") },
                //    onFailure = { Text("Failed to load") }
                // )
                Text(
                    text = item.image?.path?.takeLast(20) ?: "No Image", // Show part of path or placeholder
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            // Optional: Add subtitle (e.g., artist name if available and applicable)
            // This requires enhancing ItemMapping or having more specific types
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}