package com.aurawall.app.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface UnsplashApi {
    @GET("photos")
    suspend fun getCuratedPhotos(
        @Header("Authorization") authHeader: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("order_by") orderBy: String = "popular"
    ): List<UnsplashPhoto>

    @GET("search/photos")
    suspend fun searchPhotos(
        @Header("Authorization") authHeader: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("orientation") orientation: String? = null,
        @Query("color") color: String? = null
    ): UnsplashSearchResponse
}

data class UnsplashSearchResponse(
    val results: List<UnsplashPhoto> = emptyList()
)

data class UnsplashPhoto(
    val id: String,
    val width: Int = 1080,
    val height: Int = 2340,
    val color: String? = null,
    val urls: UnsplashUrls = UnsplashUrls(),
    val user: UnsplashUser = UnsplashUser(),
    val links: UnsplashLinks = UnsplashLinks()
) {
    fun toWallpaper(): Wallpaper = Wallpaper(
        id = "un_$id",
        source = WallpaperSource.UNSPLASH,
        thumbUrl = urls.regular.ifBlank { urls.small },
        fullUrl = urls.full.ifBlank { urls.raw.ifBlank { urls.regular } },
        width = width,
        height = height,
        colors = listOfNotNull(color),
        author = user.name.ifBlank { "Unsplash" },
        category = "photography",
        sourceUrl = links.html
    )
}

data class UnsplashUrls(
    val raw: String = "",
    val full: String = "",
    val regular: String = "",
    val small: String = ""
)

data class UnsplashUser(
    val name: String = ""
)

data class UnsplashLinks(
    @SerializedName("html") val html: String = ""
)
