package com.aurawall.app.data

enum class WallpaperSource(val label: String) {
    WALLHAVEN("Wallhaven"),
    UNSPLASH("Unsplash")
}

enum class SourcePreference(val label: String) {
    WALLHAVEN_ONLY("Wallhaven (Default)"),
    BOTH("Wallhaven + Unsplash"),
    UNSPLASH_ONLY("Unsplash Only")
}

data class Wallpaper(
    val id: String,
    val source: WallpaperSource,
    val thumbUrl: String,
    val fullUrl: String,
    val width: Int,
    val height: Int,
    val colors: List<String> = emptyList(),
    val author: String = "",
    val category: String = "",
    val sourceUrl: String = ""
) {
    val aspectRatio: Float
        get() = if (width > 0 && height > 0) {
            (width.toFloat() / height.toFloat()).coerceIn(0.45f, 1.4f)
        } else {
            0.56f
        }

    val resolutionText: String
        get() = if (width > 0 && height > 0) "${width} × ${height}" else "HD"
}

data class CategoryChip(
    val label: String,
    val query: String,
    val sorting: String = "toplist",
    val colorHex: String? = null
)

val DefaultCategories = listOf(
    CategoryChip("Trending", "", sorting = "toplist"),
    CategoryChip("AMOLED", "amoled", sorting = "toplist", colorHex = "000000"),
    CategoryChip("Minimal", "minimalism", sorting = "toplist"),
    CategoryChip("Abstract", "abstract", sorting = "toplist"),
    CategoryChip("Nature", "nature landscape", sorting = "toplist"),
    CategoryChip("Dark", "dark", sorting = "toplist", colorHex = "000000"),
    CategoryChip("Architecture", "architecture", sorting = "toplist"),
    CategoryChip("Space", "space nebula", sorting = "toplist")
)

data class ColorFilter(
    val name: String,
    val wallhavenHex: String,
    val unsplashColor: String,
    val argb: Long
)

val ColorSwatches = listOf(
    ColorFilter("AMOLED", "000000", "black", 0xFF000000),
    ColorFilter("Midnight", "0066cc", "blue", 0xFF1565C0),
    ColorFilter("Emerald", "009966", "green", 0xFF00897B),
    ColorFilter("Crimson", "cc0000", "red", 0xFFC62828),
    ColorFilter("Amber", "ff9900", "orange", 0xFFEF6C00),
    ColorFilter("Violet", "663399", "purple", 0xFF6A1B9A),
    ColorFilter("Cyan", "00cccc", "teal", 0xFF00ACC1),
    ColorFilter("Mono", "999999", "black_and_white", 0xFF757575)
)
