package com.example.passionDaily.di

import android.content.Context
import com.example.passionDaily.favorites.manager.FavoritesLoadingManager
import com.example.passionDaily.favorites.manager.FavoritesLoadingManagerImpl
import com.example.passionDaily.favorites.manager.FavoritesRemoveManager
import com.example.passionDaily.favorites.manager.FavoritesRemoveManagerImpl
import com.example.passionDaily.favorites.manager.FavoritesSavingManager
import com.example.passionDaily.favorites.manager.FavoritesSavingManagerImpl
import com.example.passionDaily.favorites.usecase.GetRequiredDataUseCase
import com.example.passionDaily.favorites.usecase.LoadFavoritesUseCase
import com.example.passionDaily.favorites.usecase.RemoveFavoritesUseCase
import com.example.passionDaily.favorites.usecase.SaveFavoritesToLocalUseCase
import com.example.passionDaily.quote.domain.usecase.QuoteListManagementUseCase
import com.example.passionDaily.quote.domain.usecase.QuoteLoadingUseCase
import com.example.passionDaily.quote.domain.usecase.QuoteStateManagementUseCase
import com.example.passionDaily.quote.manager.QuoteLoadingManager
import com.example.passionDaily.quote.manager.QuoteLoadingManagerImpl
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quotecategory.manager.QuoteCategoryManager
import com.example.passionDaily.quotecategory.manager.QuoteCategoryManagerImpl
import com.example.passionDaily.settings.domain.usecase.SendEmailUseCase
import com.example.passionDaily.settings.manager.EmailManager
import com.example.passionDaily.settings.manager.EmailManagerImpl
import com.example.passionDaily.toast.manager.ToastManager
import com.example.passionDaily.toast.manager.ToastManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataManagerModule {
    @Provides
    @Singleton
    fun provideQuoteLoadingManager(
        managementUseCase: QuoteStateManagementUseCase,
        loadingUseCase: QuoteLoadingUseCase,
        listManagementUseCase: QuoteListManagementUseCase
    ): QuoteLoadingManager = QuoteLoadingManagerImpl(
        managementUseCase,
        loadingUseCase,
        listManagementUseCase
    )

    @Provides
    @Singleton
    fun provideFavoritesLoadingManager(
        loadFavoritesUseCase: LoadFavoritesUseCase
    ): FavoritesLoadingManager = FavoritesLoadingManagerImpl(
        loadFavoritesUseCase
    )

    @Provides
    @Singleton
    fun provideFavoritesSavingManager(
        saveFavoritesToLocalUseCase: SaveFavoritesToLocalUseCase,
        getRequiredDataUseCase: GetRequiredDataUseCase
    ): FavoritesSavingManager = FavoritesSavingManagerImpl(
        saveFavoritesToLocalUseCase, getRequiredDataUseCase
    )

    @Provides
    @Singleton
    fun provideFavoritesRemoveManager(
        removeFavoritesUseCase: RemoveFavoritesUseCase
    ): FavoritesRemoveManager = FavoritesRemoveManagerImpl(
        removeFavoritesUseCase
    )

    @Provides
    @Singleton
    fun provideToastManager(
        context: Context
    ): ToastManager = ToastManagerImpl(
        context
    )

    @Provides
    @Singleton
    fun provideEmailManager(
        sendEmailUseCase: SendEmailUseCase
    ): EmailManager = EmailManagerImpl(
        sendEmailUseCase
    )

    @Provides
    @Singleton
    fun provideQuoteCategoryManager(
        quoteStateHolder: QuoteStateHolder
    ): QuoteCategoryManager = QuoteCategoryManagerImpl(quoteStateHolder)
}