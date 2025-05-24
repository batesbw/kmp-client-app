package com.mass.client.feature_home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mass.client.core.model.Album
import com.mass.client.core.model.Artist
import com.mass.client.feature_home.viewmodel.HomeViewModel
import com.mass.client.feature_home.ui.components.HomeTopAppBar
import com.mass.client.feature_home.ui.components.MusicAssistantTitle
import com.mass.client.feature_home.ui.components.PlayersSection
import com.mass.client.feature_home.ui.components.RecentlyPlayedSection
import com.mass.client.feature_home.ui.components.RandomArtistsSectionPlaceholder

// Placeholder data classes were here, now removed as we use models from MediaItems.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onArtistClick: (Artist) -> Unit,
    onAlbumClick: (Album) -> Unit,
    paddingValues: PaddingValues = PaddingValues()
) {
    val recentlyPlayedItems by viewModel.recentlyPlayedItems.collectAsState()
    // val artists by viewModel.artists.collectAsState()
    // val albums by viewModel.albums.collectAsState()

    Scaffold(
        topBar = {
            HomeTopAppBar(
                onMenuClick = { println("Menu clicked") /* TODO */ },
                onOverflowClick = { println("Overflow clicked") /* TODO */ }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { MusicAssistantTitle() }
            item { PlayersSection(homeViewModel = viewModel) }
            item { 
                RecentlyPlayedSection(
                    recentlyPlayedItems = recentlyPlayedItems,
                    onItemClick = viewModel::onRecentlyPlayedItemClicked
                ) 
            }
            item { RandomArtistsSectionPlaceholder() }
        }
    }
}

// Removed old HorizontalArtistList, HorizontalAlbumList, ArtistCard, AlbumCard definitions
// as they were causing build errors and are part of the old design. 