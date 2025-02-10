package com.example.passionDaily.favorites.state

import com.example.passionDaily.favorites.base.FavoritesViewModelState
import kotlinx.coroutines.flow.StateFlow

interface FavoriteErrorState : FavoritesViewModelState {
    override val error: StateFlow<String?>
}