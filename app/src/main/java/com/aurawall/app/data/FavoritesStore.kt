package com.aurawall.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "aurawall_prefs")

class FavoritesStore(private val context: Context) {
    private val gson = Gson()
    private val favoritesKey = stringPreferencesKey("favorites_json")
    private val unsplashKeyPref = stringPreferencesKey("unsplash_api_key")
    private val sourcePrefKey = stringPreferencesKey("source_preference")

    val favoritesFlow: Flow<List<Wallpaper>> = context.dataStore.data.map { prefs ->
        val json = prefs[favoritesKey] ?: return@map emptyList()
        runCatching {
            val type = object : TypeToken<List<Wallpaper>>() {}.type
            gson.fromJson<List<Wallpaper>>(json, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    val unsplashApiKeyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[unsplashKeyPref] ?: ""
    }

    val sourcePreferenceFlow: Flow<SourcePreference> = context.dataStore.data.map { prefs ->
        val name = prefs[sourcePrefKey] ?: SourcePreference.WALLHAVEN_ONLY.name
        runCatching { SourcePreference.valueOf(name) }.getOrDefault(SourcePreference.WALLHAVEN_ONLY)
    }

    suspend fun toggleFavorite(wallpaper: Wallpaper) {
        context.dataStore.edit { prefs ->
            val current = runCatching {
                val json = prefs[favoritesKey] ?: return@runCatching emptyList<Wallpaper>()
                val type = object : TypeToken<List<Wallpaper>>() {}.type
                gson.fromJson<List<Wallpaper>>(json, type) ?: emptyList()
            }.getOrDefault(emptyList()).toMutableList()

            val existingIndex = current.indexOfFirst { it.id == wallpaper.id }
            if (existingIndex >= 0) {
                current.removeAt(existingIndex)
            } else {
                current.add(0, wallpaper)
            }
            prefs[favoritesKey] = gson.toJson(current)
        }
    }

    suspend fun saveUnsplashApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[unsplashKeyPref] = key.trim()
        }
    }

    suspend fun saveSourcePreference(pref: SourcePreference) {
        context.dataStore.edit { prefs ->
            prefs[sourcePrefKey] = pref.name
        }
    }
}
