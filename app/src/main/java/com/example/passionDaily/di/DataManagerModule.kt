package com.example.passionDaily.di

import com.example.passionDaily.quote.domain.usecase.QuoteListManagementUseCase
import com.example.passionDaily.quote.domain.usecase.QuoteLoadingUseCase
import com.example.passionDaily.quote.domain.usecase.QuoteStateManagementUseCase
import com.example.passionDaily.quote.manager.QuoteLoadingManager
import com.example.passionDaily.quote.manager.QuoteLoadingManagerImpl
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quotecategory.manager.QuoteCategoryManager
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
    fun provideQuoteCategoryManager(
        quoteStateHolder: QuoteStateHolder
    ): QuoteCategoryManager = QuoteCategoryManager(quoteStateHolder)
}