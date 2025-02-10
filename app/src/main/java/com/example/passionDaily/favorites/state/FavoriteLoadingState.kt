package com.example.passionDaily.favorites.state

import com.example.passionDaily.favorites.base.FavoritesViewModelState
import kotlinx.coroutines.flow.StateFlow

interface FavoriteLoadingState : FavoritesViewModelState {
    override val isFavoriteLoading: StateFlow<Boolean>
}
