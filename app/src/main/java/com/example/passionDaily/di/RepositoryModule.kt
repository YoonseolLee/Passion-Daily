package com.example.passionDaily.di

import com.example.passionDaily.data.repository.local.LocalFavoriteRepository
import com.example.passionDaily.data.repository.local.LocalFavoriteRepositoryImpl
import com.example.passionDaily.data.repository.local.LocalQuoteCategoryRepository
import com.example.passionDaily.data.repository.local.LocalQuoteCategoryRepositoryImpl
import com.example.passionDaily.quote.data.local.LocalQuoteRepository
import com.example.passionDaily.quote.data.local.LocalQuoteRepositoryImpl
import com.example.passionDaily.data.repository.local.LocalUserRepository
import com.example.passionDaily.data.repository.local.LocalUserRepositoryImpl
import com.example.passionDaily.data.repository.remote.RemoteFavoriteRepository
import com.example.passionDaily.data.repository.remote.RemoteFavoriteRepositoryImpl
import com.example.passionDaily.quote.data.remote.RemoteQuoteRepository
import com.example.passionDaily.quote.data.remote.RemoteQuoteRepositoryImpl
import com.example.passionDaily.data.repository.remote.RemoteUserRepository
import com.example.passionDaily.data.repository.remote.RemoteUserRepositoryImpl
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
    abstract fun bindRemoteFavoriteRepository(
        repository: RemoteFavoriteRepositoryImpl
    ): RemoteFavoriteRepository

    @Binds
    abstract fun bindLocalQuoteRepository(
        repository: LocalQuoteRepositoryImpl
    ): LocalQuoteRepository

    @Binds
    abstract fun bindLocalQuoteCategoryRepository(
        repository: LocalQuoteCategoryRepositoryImpl
    ): LocalQuoteCategoryRepository

    @Binds
    abstract fun bindLocalUserRepository(
        repository: LocalUserRepositoryImpl
    ): LocalUserRepository

    @Binds
    abstract fun bindRemoteUserRepository(
        repository: RemoteUserRepositoryImpl
    ): RemoteUserRepository
}