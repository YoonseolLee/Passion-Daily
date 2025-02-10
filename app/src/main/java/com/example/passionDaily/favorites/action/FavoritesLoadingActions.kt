package com.example.passionDaily.favorites.action

import com.example.passionDaily.favorites.base.FavoritesViewModelActions

interface FavoritesLoadingActions : FavoritesViewModelActions{
    override fun loadFavorites()
}