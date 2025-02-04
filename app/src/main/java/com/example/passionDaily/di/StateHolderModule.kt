package com.example.passionDaily.di

import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quote.stateholder.QuoteStateHolderImpl
import com.example.passionDaily.settings.stateholder.SettingsStateHolderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.passionDaily.favorites.stateholder.FavoritesStateHolder
import com.example.passionDaily.favorites.stateholder.FavoritesStateHolderImpl
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.login.stateholder.AuthStateHolderImpl
import com.example.passionDaily.login.stateholder.ConsentStateHolder
import com.example.passionDaily.login.stateholder.ConsentStateHolderImpl
import com.example.passionDaily.login.stateholder.LoginStateHolder
import com.example.passionDaily.login.stateholder.LoginStateHolderImpl
import com.example.passionDaily.login.stateholder.UserProfileStateHolder
import com.example.passionDaily.login.stateholder.UserProfileStateHolderImpl
import com.example.passionDaily.settings.stateholder.SettingsStateHolder

@Module
@InstallIn(SingletonComponent::class)
object StateHolderModule {
    @Provides
    @Singleton
    fun provideQuoteStateHolder(): QuoteStateHolder = QuoteStateHolderImpl()

    @Provides
    @Singleton
    fun provideSettingsStateHolder(): SettingsStateHolder = SettingsStateHolderImpl()

    @Provides
    @Singleton
    fun provideAuthStateHolder(): AuthStateHolder = AuthStateHolderImpl()

    @Provides
    @Singleton
    fun provideLoginStateHolder(): LoginStateHolder = LoginStateHolderImpl()

    @Provides
    @Singleton
    fun provideUserProfileStateHolder(): UserProfileStateHolder = UserProfileStateHolderImpl()

    @Provides
    @Singleton
    fun provideConsentStateHolder(): ConsentStateHolder = ConsentStateHolderImpl()

    @Provides
    @Singleton
    fun provideFavoritesStateHolder(): FavoritesStateHolder =
        FavoritesStateHolderImpl().also { it.updateFavoriteQuotes(emptyList()) }
}