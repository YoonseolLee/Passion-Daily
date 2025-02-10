package com.example.passionDaily.favorites.action

import com.example.passionDaily.favorites.base.FavoritesViewModelActions

interface FavoritesModificationActions : FavoritesViewModelActions {
    override fun addFavorite(quoteId: String)
    override suspend fun removeFavorite(quoteId: String, categoryId: Int)
}