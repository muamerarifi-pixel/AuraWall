package com.aurawall.app.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class WallpaperRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val wallhavenApi: WallhavenApi = Retrofit.Builder()
        .baseUrl("https://wallhaven.cc/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WallhavenApi::class.java)

    private val unsplashApi: UnsplashApi = Retrofit.Builder()
        .baseUrl("https://api.unsplash.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(UnsplashApi::class.java)

    suspend fun fetchWallpapers(
        query: String = "",
        sorting: String = "toplist",
        portraitOnly: Boolean = true,
        colorFilter: ColorFilter? = null,
        page: Int = 1,
        unsplashApiKey: String = "",
        sourcePreference: SourcePreference = SourcePreference.WALLHAVEN_ONLY
    ): List<Wallpaper> {
        val canUseUnsplash = unsplashApiKey.isNotBlank() &&
            (sourcePreference == SourcePreference.BOTH || sourcePreference == SourcePreference.UNSPLASH_ONLY)
        val useWallhaven = sourcePreference != SourcePreference.UNSPLASH_ONLY || !canUseUnsplash

        val results = mutableListOf<Wallpaper>()

        if (useWallhaven) {
            runCatching {
                val response = wallhavenApi.search(
                    query = query.ifBlank { null },
                    sorting = sorting,
                    ratios = if (portraitOnly) "portrait" else null,
                    colors = colorFilter?.wallhavenHex,
                    page = page
                )
                results.addAll(response.data.map { it.toWallpaper() })
            }
        }

        if (canUseUnsplash) {
            runCatching {
                val auth = "Client-ID $unsplashApiKey"
                val unsplashItems = if (query.isBlank() && colorFilter == null && !portraitOnly) {
                    unsplashApi.getCuratedPhotos(authHeader = auth, page = page)
                } else {
                    val effectiveQuery = query.ifBlank { "wallpaper aesthetic" }
                    unsplashApi.searchPhotos(
                        authHeader = auth,
                        query = effectiveQuery,
                        page = page,
                        orientation = if (portraitOnly) "portrait" else null,
                        color = colorFilter?.unsplashColor
                    ).results
                }
                results.addAll(unsplashItems.map { it.toWallpaper() })
            }
        }

        return if (useWallhaven && canUseUnsplash) {
            // Interleave results nicely when both sources are active
            val wh = results.filter { it.source == WallpaperSource.WALLHAVEN }
            val un = results.filter { it.source == WallpaperSource.UNSPLASH }
            val merged = mutableListOf<Wallpaper>()
            val maxLen = maxOf(wh.size, un.size)
            for (i in 0 until maxLen) {
                if (i < wh.size) merged.add(wh[i])
                if (i < un.size) merged.add(un[i])
            }
            merged
        } else {
            results
        }
    }
}
