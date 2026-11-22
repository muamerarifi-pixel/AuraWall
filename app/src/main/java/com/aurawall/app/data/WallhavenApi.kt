package com.aurawall.app.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface WallhavenApi {
    @GET("api/v1/search")
    suspend fun search(
        @Query("q") query: String? = null,
        @Query("categories") categories: String = "110",
        @Query("purity") purity: String = "100",
        @Query("sorting") sorting: String = "toplist",
        @Query("order") order: String = "desc",
        @Query("topRange") topRange: String = "1M",
        @Query("ratios") ratios: String? = null,
        @Query("colors") colors: String? = null,
        @Query("page") page: Int = 1
    ): WallhavenResponse
}

data class WallhavenResponse(
    val data: List<WallhavenItem> = emptyList()
)

data class WallhavenItem(
    val id: String,
    val url: String = "",
    val category: String = "",
    @SerializedName("dimension_x") val width: Int = 1080,
    @SerializedName("dimension_y") val height: Int = 2340,
    val colors: List<String> = emptyList(),
    val path: String = "",
    val thumbs: WallhavenThumbs = WallhavenThumbs()
) {
    fun toWallpaper(): Wallpaper = Wallpaper(
        id = "wh_$id",
        source = WallpaperSource.WALLHAVEN,
        thumbUrl = thumbs.large.ifBlank { thumbs.original.ifBlank { path } },
        fullUrl = path,
        width = width,
        height = height,
        colors = colors,
        author = "Wallhaven",
        category = category,
        sourceUrl = url
    )
}

data class WallhavenThumbs(
    val large: String = "",
    val original: String = "",
    val small: String = ""
)
