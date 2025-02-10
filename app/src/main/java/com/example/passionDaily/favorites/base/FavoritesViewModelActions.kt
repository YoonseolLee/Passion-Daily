package com.example.passionDaily.favorites.base

interface FavoritesViewModelActions {
    fun previousQuote()
    fun nextQuote()
    fun loadFavorites()
    fun addFavorite(quoteId: String)
    suspend fun removeFavorite(quoteId: String, categoryId: Int)
}