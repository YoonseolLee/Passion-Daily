package com.example.passionDaily.di

import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepositoryImpl
import com.example.passionDaily.quote.data.local.repository.LocalQuoteRepository
import com.example.passionDaily.quote.data.local.repository.LocalQuoteRepositoryImpl
import com.example.passionDaily.quote.data.remote.repository.RemoteQuoteRepository
import com.example.passionDaily.quote.data.remote.repository.RemoteQuoteRepositoryImpl
import com.example.passionDaily.quotecategory.data.local.repository.LocalQuoteCategoryRepository
import com.example.passionDaily.quotecategory.data.local.repository.LocalQuoteCategoryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindLocalFavoriteRepository(
        repository: LocalFavoriteRepositoryImpl
    ): LocalFavoriteRepository

    @Binds
    abstract fun bindRemoteQuoteRepository(
        repository: RemoteQuoteRepositoryImpl
    ): RemoteQuoteRepository

    @Binds
    abstract fun bindLocalQuoteRepository(
        repository: LocalQuoteRepositoryImpl
    ): LocalQuoteRepository

    @Binds
    abstract fun bindLocalQuoteCategoryRepository(
        repository: LocalQuoteCategoryRepositoryImpl
    ): LocalQuoteCategoryRepository
}