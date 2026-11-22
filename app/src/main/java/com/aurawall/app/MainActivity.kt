package com.aurawall.app

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aurawall.app.data.Wallpaper
import com.aurawall.app.ui.AuraWallViewModel
import com.aurawall.app.ui.components.GlassPill
import com.aurawall.app.ui.screens.ExploreScreen
import com.aurawall.app.ui.screens.FavoritesScreen
import com.aurawall.app.ui.screens.PreviewScreen
import com.aurawall.app.ui.screens.SearchScreen
import com.aurawall.app.ui.screens.SettingsScreen
import com.aurawall.app.ui.theme.AuraWallTheme

enum class AppTab(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    EXPLORE("Explore", Icons.Outlined.Explore, Icons.Rounded.Explore),
    SEARCH("Search", Icons.Outlined.Search, Icons.Rounded.Search),
    FAVORITES("Favorites", Icons.Outlined.FavoriteBorder, Icons.Rounded.Favorite),
    SETTINGS("Settings", Icons.Outlined.Settings, Icons.Rounded.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            AuraWallTheme {
                AuraWallApp()
            }
        }
    }
}

@Composable
private fun AuraWallApp(
    viewModel: AuraWallViewModel = viewModel()
) {
    val exploreState by viewModel.exploreState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val unsplashApiKey by viewModel.unsplashApiKey.collectAsStateWithLifecycle()
    val sourcePreference by viewModel.sourcePreference.collectAsStateWithLifecycle()

    val favoriteIds = remember(favorites) { favorites.map { it.id }.toSet() }
    var currentTab by remember { mutableStateOf(AppTab.EXPLORE) }
    var activePreview by remember { mutableStateOf<Wallpaper?>(null) }

    BackHandler(enabled = activePreview != null) {
        activePreview = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
            when (tab) {
                AppTab.EXPLORE -> ExploreScreen(
                    state = exploreState,
                    favoriteIds = favoriteIds,
                    onCategorySelected = viewModel::selectCategory,
                    onTogglePortrait = viewModel::toggleExplorePortrait,
                    onLoadMore = { viewModel.loadExplore(reset = false) },
                    onRetry = { viewModel.loadExplore(reset = true) },
                    onWallpaperClick = { activePreview = it }
                )
                AppTab.SEARCH -> SearchScreen(
                    state = searchState,
                    favoriteIds = favoriteIds,
                    onQueryChange = viewModel::updateSearchQuery,
                    onSearch = { viewModel.performSearch(reset = true) },
                    onColorSelected = viewModel::selectSearchColor,
                    onLoadMore = { viewModel.performSearch(reset = false) },
                    onWallpaperClick = { activePreview = it }
                )
                AppTab.FAVORITES -> FavoritesScreen(
                    favorites = favorites,
                    favoriteIds = favoriteIds,
                    onWallpaperClick = { activePreview = it }
                )
                AppTab.SETTINGS -> SettingsScreen(
                    unsplashApiKey = unsplashApiKey,
                    sourcePreference = sourcePreference,
                    onSaveUnsplashKey = viewModel::saveUnsplashApiKey,
                    onSelectSource = viewModel::saveSourcePreference
                )
            }
        }

        // Floating translucent glass bottom navigation pill
        GlassPill(
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 18.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTab.entries.forEach { tab ->
                    val selected = tab == currentTab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                else Color.Transparent
                            )
                            .clickable { currentTab = tab }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFB4B4BE),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Full-screen preview overlay
        activePreview?.let { wallpaper ->
            PreviewScreen(
                wallpaper = wallpaper,
                isFavorite = favoriteIds.contains(wallpaper.id),
                onBack = { activePreview = null },
                onToggleFavorite = { viewModel.toggleFavorite(wallpaper) }
            )
        }
    }
}
