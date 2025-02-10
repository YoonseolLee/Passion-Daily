package com.example.passionDaily.favorites.action

import com.example.passionDaily.favorites.base.FavoritesViewModelActions

interface FavoritesNavigationActions : FavoritesViewModelActions {
    override fun previousQuote()
    override fun nextQuote()
}
