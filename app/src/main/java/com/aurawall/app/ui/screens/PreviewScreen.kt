package com.aurawall.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.aurawall.app.data.Wallpaper
import com.aurawall.app.ui.components.GlassPill
import com.aurawall.app.util.WallpaperApplier
import com.aurawall.app.util.WallpaperTarget
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    wallpaper: Wallpaper,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showChrome by remember { mutableStateOf(true) }
    var showApplySheet by remember { mutableStateOf(false) }
    var isWorking by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showChrome = !showChrome }
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(wallpaper.fullUrl)
                .crossfade(300)
                .build(),
            contentDescription = "Wallpaper Preview",
            contentScale = ContentScale.Crop,
            loading = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.5.dp
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top floating chrome
        AnimatedVisibility(
            visible = showChrome,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassPill(shape = CircleShape) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }

                GlassPill(shape = RoundedCornerShape(20.dp)) {
                    Text(
                        text = "${wallpaper.resolutionText} • ${wallpaper.source.label}",
                        color = Color(0xFFE6E6EE),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                GlassPill(shape = CircleShape) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color(0xFFFF5252) else Color.White
                        )
                    }
                }
            }
        }

        // Bottom floating glass action pill
        AnimatedVisibility(
            visible = showChrome,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            GlassPill(shape = RoundedCornerShape(32.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Apply button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(enabled = !isWorking) { showApplySheet = true }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isWorking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Wallpaper,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = if (isWorking) "Applying..." else "Apply",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Save to Gallery button
                    IconButton(
                        enabled = !isWorking,
                        onClick = {
                            scope.launch {
                                isWorking = true
                                val result = WallpaperApplier.saveToGallery(
                                    context = context,
                                    imageUrl = wallpaper.fullUrl,
                                    fileName = "AuraWall_${wallpaper.id}"
                                )
                                isWorking = false
                                val msg = if (result.isSuccess) {
                                    "Saved to Pictures/AuraWall"
                                } else {
                                    "Failed to save wallpaper"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Save to Gallery",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    if (showApplySheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showApplySheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF121216),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Set Wallpaper",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(14.dp))

                ApplyOptionRow(
                    icon = Icons.Outlined.Home,
                    label = WallpaperTarget.HOME.label,
                    onClick = {
                        showApplySheet = false
                        scope.launch {
                            isWorking = true
                            val res = WallpaperApplier.applyWallpaper(context, wallpaper.fullUrl, WallpaperTarget.HOME)
                            isWorking = false
                            Toast.makeText(
                                context,
                                if (res.isSuccess) "Applied to Home Screen" else "Failed to apply wallpaper",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                ApplyOptionRow(
                    icon = Icons.Outlined.Lock,
                    label = WallpaperTarget.LOCK.label,
                    onClick = {
                        showApplySheet = false
                        scope.launch {
                            isWorking = true
                            val res = WallpaperApplier.applyWallpaper(context, wallpaper.fullUrl, WallpaperTarget.LOCK)
                            isWorking = false
                            Toast.makeText(
                                context,
                                if (res.isSuccess) "Applied to Lock Screen" else "Failed to apply wallpaper",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                ApplyOptionRow(
                    icon = Icons.Outlined.Smartphone,
                    label = WallpaperTarget.BOTH.label,
                    onClick = {
                        showApplySheet = false
                        scope.launch {
                            isWorking = true
                            val res = WallpaperApplier.applyWallpaper(context, wallpaper.fullUrl, WallpaperTarget.BOTH)
                            isWorking = false
                            Toast.makeText(
                                context,
                                if (res.isSuccess) "Applied to Home & Lock Screen" else "Failed to apply wallpaper",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ApplyOptionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
