package com.example.passionDaily.di

import com.example.passionDaily.quote.domain.usecase.ImageShareUseCase
import com.example.passionDaily.quote.domain.usecase.IncrementShareCountUseCase
import com.example.passionDaily.quote.manager.ShareQuoteManager
import com.example.passionDaily.quote.manager.ShareQuoteManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilityModule {
    @Provides
    @Singleton
    fun provideShareQuoteManager(
        imageShareUseCase: ImageShareUseCase,
        incrementShareCountUseCase: IncrementShareCountUseCase
    ): ShareQuoteManager = ShareQuoteManagerImpl(
        imageShareUseCase,
        incrementShareCountUseCase
    )
}