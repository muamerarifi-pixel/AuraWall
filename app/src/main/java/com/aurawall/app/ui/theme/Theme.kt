package com.aurawall.app.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val AmoledFallbackScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF001D35),
    secondary = Color(0xFFC58AF9),
    onSecondary = Color(0xFF2B0052),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF5F5F7),
    surface = Color(0xFF0A0A0C),
    onSurface = Color(0xFFF5F5F7),
    surfaceVariant = Color(0xFF16161A),
    onSurfaceVariant = Color(0xFFB4B4BE),
    outline = Color(0xFF282830)
)

@Composable
fun AuraWallTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Sync accent colors with One UI 9 / Material You palette while enforcing true AMOLED black
        dynamicDarkColorScheme(context).copy(
            background = Color(0xFF000000),
            surface = Color(0xFF0A0A0C),
            surfaceVariant = Color(0xFF16161A)
        )
    } else {
        AmoledFallbackScheme
    }

    MaterialTheme(
        colorScheme = baseScheme,
        content = content
    )
}
