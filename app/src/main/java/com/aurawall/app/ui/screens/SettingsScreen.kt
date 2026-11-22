package com.aurawall.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurawall.app.data.SourcePreference

@Composable
fun SettingsScreen(
    unsplashApiKey: String,
    sourcePreference: SourcePreference,
    onSaveUnsplashKey: (String) -> Unit,
    onSelectSource: (SourcePreference) -> Unit
) {
    var keyDraft by remember(unsplashApiKey) { mutableStateOf(unsplashApiKey) }
    var savedBanner by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        // Wallpaper Source Card
        Surface(
            color = Color(0xFF111115),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Wallpaper Source",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Wallhaven works out of the box with no API key. Add an optional Unsplash Access Key below to unlock Unsplash.",
                    color = Color(0xFF9494A2),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                SourcePreference.entries.forEach { pref ->
                    val enabled = pref == SourcePreference.WALLHAVEN_ONLY || unsplashApiKey.isNotBlank()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = enabled) { onSelectSource(pref) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sourcePreference == pref,
                            onClick = { if (enabled) onSelectSource(pref) },
                            enabled = enabled
                        )
                        Text(
                            text = pref.label,
                            color = if (enabled) Color.White else Color(0xFF555560),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Optional Unsplash API Key Card
        Surface(
            color = Color(0xFF111115),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Unsplash Access Key (Optional)",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = keyDraft,
                    onValueChange = {
                        keyDraft = it
                        savedBanner = false
                    },
                    placeholder = { Text("Paste your Unsplash Access Key", color = Color(0xFF666672)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFF2C2C36)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            onSaveUnsplashKey(keyDraft)
                            savedBanner = true
                        }
                    ) {
                        Text("Save Key")
                    }
                    if (savedBanner) {
                        Text(
                            text = "Saved",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Device optimization note
        Surface(
            color = Color(0xFF111115),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "AuraWall for Galaxy S23",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Optimized for 6.1\" Dynamic AMOLED 2X (1080 × 2340, 120Hz) with true-black surfaces and One UI 9 dynamic color accents.",
                    color = Color(0xFF9494A2),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}
