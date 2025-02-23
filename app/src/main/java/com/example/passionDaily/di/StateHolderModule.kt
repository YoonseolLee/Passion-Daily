package com.example.passionDaily.di

import com.example.passionDaily.favorites.stateholder.FavoritesStateHolder
import com.example.passionDaily.favorites.stateholder.FavoritesStateHolderImpl
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quote.stateholder.QuoteStateHolderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StateHolderModule {
    @Provides
    @Singleton
    fun provideQuoteStateHolder(): QuoteStateHolder = QuoteStateHolderImpl()

    @Provides
    @Singleton
    fun provideFavoritesStateHolder(): FavoritesStateHolder =
        FavoritesStateHolderImpl().also { it.updateFavoriteQuotes(emptyList()) }
}