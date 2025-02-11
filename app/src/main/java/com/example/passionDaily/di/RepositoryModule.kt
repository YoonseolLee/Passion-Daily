package com.example.passionDaily.di

import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepositoryImpl
import com.example.passionDaily.quotecategory.data.local.repository.LocalQuoteCategoryRepository
import com.example.passionDaily.quotecategory.data.local.repository.LocalQuoteCategoryRepositoryImpl
import com.example.passionDaily.quote.data.local.repository.LocalQuoteRepository
import com.example.passionDaily.quote.data.local.repository.LocalQuoteRepositoryImpl
import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.user.data.local.repository.LocalUserRepositoryImpl
import com.example.passionDaily.favorites.data.remote.repository.RemoteFavoriteRepository
import com.example.passionDaily.favorites.data.remote.repository.RemoteFavoriteRepositoryImpl
import com.example.passionDaily.notification.data.repository.remote.UserNotificationRepository
import com.example.passionDaily.notification.data.repository.remote.UserNotificationRepositoryImpl
import com.example.passionDaily.quote.data.remote.RemoteQuoteRepository
import com.example.passionDaily.quote.data.remote.RemoteQuoteRepositoryImpl
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepositoryImpl
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

    @Binds
    abstract fun bindUserNotificationRepository(
        repository: UserNotificationRepositoryImpl
    ): UserNotificationRepository
}