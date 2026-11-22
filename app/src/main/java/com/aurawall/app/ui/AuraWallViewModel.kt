package com.aurawall.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aurawall.app.data.CategoryChip
import com.aurawall.app.data.ColorFilter
import com.aurawall.app.data.DefaultCategories
import com.aurawall.app.data.FavoritesStore
import com.aurawall.app.data.SourcePreference
import com.aurawall.app.data.Wallpaper
import com.aurawall.app.data.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val wallpapers: List<Wallpaper> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val selectedCategory: CategoryChip = DefaultCategories.first(),
    val portraitOnly: Boolean = true
)

data class SearchUiState(
    val query: String = "",
    val selectedColor: ColorFilter? = null,
    val portraitOnly: Boolean = true,
    val results: List<Wallpaper> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val page: Int = 1,
    val hasSearched: Boolean = false
)

class AuraWallViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WallpaperRepository()
    private val store = FavoritesStore(application)

    val favorites: StateFlow<List<Wallpaper>> = store.favoritesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unsplashApiKey: StateFlow<String> = store.unsplashApiKeyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val sourcePreference: StateFlow<SourcePreference> = store.sourcePreferenceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SourcePreference.WALLHAVEN_ONLY)

    private val _exploreState = MutableStateFlow(FeedUiState())
    val exploreState: StateFlow<FeedUiState> = _exploreState.asStateFlow()

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    init {
        loadExplore(reset = true)
    }

    fun selectCategory(category: CategoryChip) {
        if (_exploreState.value.selectedCategory == category) return
        _exploreState.update { it.copy(selectedCategory = category) }
        loadExplore(reset = true)
    }

    fun toggleExplorePortrait() {
        _exploreState.update { it.copy(portraitOnly = !it.portraitOnly) }
        loadExplore(reset = true)
    }

    fun loadExplore(reset: Boolean = false) {
        val current = _exploreState.value
        if (current.isLoading || current.isLoadingMore) return
        val nextPage = if (reset) 1 else current.page + 1

        viewModelScope.launch {
            _exploreState.update {
                if (reset) it.copy(isLoading = true, error = null)
                else it.copy(isLoadingMore = true, error = null)
            }

            val cat = _exploreState.value.selectedCategory
            val colorFilter = cat.colorHex?.let {
                ColorFilter("Category", it, "black", 0xFF000000)
            }

            val fetched = repository.fetchWallpapers(
                query = cat.query,
                sorting = cat.sorting,
                portraitOnly = _exploreState.value.portraitOnly,
                colorFilter = colorFilter,
                page = nextPage,
                unsplashApiKey = unsplashApiKey.value,
                sourcePreference = sourcePreference.value
            )

            _exploreState.update { state ->
                val combined = if (reset) fetched else (state.wallpapers + fetched).distinctBy { it.id }
                state.copy(
                    wallpapers = combined,
                    isLoading = false,
                    isLoadingMore = false,
                    page = nextPage,
                    error = if (combined.isEmpty()) "No wallpapers found. Check your connection and try again." else null
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchState.update { it.copy(query = query) }
    }

    fun selectSearchColor(color: ColorFilter?) {
        val toggled = if (_searchState.value.selectedColor == color) null else color
        _searchState.update { it.copy(selectedColor = toggled) }
        performSearch(reset = true)
    }

    fun performSearch(reset: Boolean = true) {
        val current = _searchState.value
        if (current.isLoading || current.isLoadingMore) return
        val nextPage = if (reset) 1 else current.page + 1

        viewModelScope.launch {
            _searchState.update {
                if (reset) it.copy(isLoading = true, hasSearched = true)
                else it.copy(isLoadingMore = true)
            }

            val fetched = repository.fetchWallpapers(
                query = _searchState.value.query,
                sorting = "relevance",
                portraitOnly = _searchState.value.portraitOnly,
                colorFilter = _searchState.value.selectedColor,
                page = nextPage,
                unsplashApiKey = unsplashApiKey.value,
                sourcePreference = sourcePreference.value
            )

            _searchState.update { state ->
                val combined = if (reset) fetched else (state.results + fetched).distinctBy { it.id }
                state.copy(
                    results = combined,
                    isLoading = false,
                    isLoadingMore = false,
                    page = nextPage
                )
            }
        }
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch {
            store.toggleFavorite(wallpaper)
        }
    }

    fun saveUnsplashApiKey(key: String) {
        viewModelScope.launch {
            store.saveUnsplashApiKey(key)
        }
    }

    fun saveSourcePreference(pref: SourcePreference) {
        viewModelScope.launch {
            store.saveSourcePreference(pref)
            loadExplore(reset = true)
        }
    }
}
