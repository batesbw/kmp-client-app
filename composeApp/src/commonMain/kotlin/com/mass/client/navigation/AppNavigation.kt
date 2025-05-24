package com.mass.client.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.mass.client.feature_home.ui.HomeScreen // Will be created/updated
import com.mass.client.feature_home.viewmodel.HomeViewModel
import com.mass.client.feature_library.ui.LibraryScreen // Will be created
import com.mass.client.feature_search.ui.SearchScreen // Will be created
import org.koin.compose.koinInject // Import for koinInject

object HomeTab : Tab {
    @Composable
    override fun Content() {
        val homeViewModel: HomeViewModel = koinInject()
        HomeScreen(
            viewModel = homeViewModel,
            // Add onArtistClick and onAlbumClick if they are still relevant for the new design
            // For now, let's assume they might be handled differently or within HomeScreen itself
            onArtistClick = { println("Artist clicked in new HomeTab: ${it.name}") },
            onAlbumClick = { println("Album clicked in new HomeTab: ${it.name}") }
        )
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "Home"
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }
}

object SearchTab : Tab {
    @Composable
    override fun Content() {
        SearchScreen()
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "Search"
            val icon = rememberVectorPainter(Icons.Default.Search)
            return remember {
                TabOptions(
                    index = 1u,
                    title = title,
                    icon = icon
                )
            }
        }
}

object LibraryTab : Tab {
    @Composable
    override fun Content() {
        LibraryScreen()
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "Library"
            val icon = rememberVectorPainter(Icons.Default.LibraryMusic)
            return remember {
                TabOptions(
                    index = 2u,
                    title = title,
                    icon = icon
                )
            }
        }
} 