package com.aurawall.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurawall.app.data.CategoryChip
import com.aurawall.app.data.DefaultCategories
import com.aurawall.app.data.Wallpaper
import com.aurawall.app.ui.FeedUiState
import com.aurawall.app.ui.components.GlassPill
import com.aurawall.app.ui.components.WallpaperMasonryGrid

@Composable
fun ExploreScreen(
    state: FeedUiState,
    favoriteIds: Set<String>,
    onCategorySelected: (CategoryChip) -> Unit,
    onTogglePortrait: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onWallpaperClick: (Wallpaper) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            state.isLoading && state.wallpapers.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.5.dp
                    )
                }
            }
            state.error != null && state.wallpapers.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.error,
                        color = Color(0xFFB4B4BE),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
            else -> {
                WallpaperMasonryGrid(
                    wallpapers = state.wallpapers,
                    favoriteIds = favoriteIds,
                    isLoadingMore = state.isLoadingMore,
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 110.dp,
                        bottom = 112.dp
                    ),
                    onWallpaperClick = onWallpaperClick,
                    onLoadMore = onLoadMore
                )
            }
        }

        // Subtle top gradient scrim so floating glass chips stay legible over bright wallpapers
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xDD000000), Color.Transparent)
                    )
                )
        )

        // Floating minimal top bar: S23 Portrait pill + category chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                val active = state.portraitOnly
                GlassPill(
                    shape = RoundedCornerShape(22.dp),
                    containerColor = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.24f) else Color(0xBF121216),
                    borderColor = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else Color(0x33FFFFFF)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .clickable(onClick = onTogglePortrait)
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Smartphone,
                            contentDescription = "S23 Portrait Filter",
                            tint = if (active) MaterialTheme.colorScheme.primary else Color(0xFFB4B4BE),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "S23",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (active) MaterialTheme.colorScheme.primary else Color(0xFFD0D0DA)
                        )
                    }
                }
            }

            items(DefaultCategories) { category ->
                val selected = state.selectedCategory == category
                GlassPill(
                    shape = RoundedCornerShape(22.dp),
                    containerColor = if (selected) Color(0xF0FFFFFF) else Color(0xBF121216),
                    borderColor = if (selected) Color.White else Color(0x2EFFFFFF)
                ) {
                    Text(
                        text = category.label,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) Color.Black else Color(0xFFE6E6EE),
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .clickable { onCategorySelected(category) }
                            .padding(horizontal = 15.dp, vertical = 9.dp)
                    )
                }
            }
        }
    }
}
