package com.example.passionDaily.di

import android.content.Context
import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
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
import com.example.passionDaily.favorites.usecase.SaveFavoritesToRemoteUseCase
import com.example.passionDaily.login.domain.usecase.CreateInitialProfileUseCase
import com.example.passionDaily.login.domain.usecase.GetFirebaseUserUseCase
import com.example.passionDaily.login.domain.usecase.GetGoogleCredentialUseCase
import com.example.passionDaily.login.domain.usecase.ManageJsonUseCase
import com.example.passionDaily.login.domain.usecase.SaveUserProfileUseCase
import com.example.passionDaily.login.manager.AuthenticationManager
import com.example.passionDaily.login.manager.AuthenticationManagerImpl
import com.example.passionDaily.login.manager.UrlManager
import com.example.passionDaily.login.manager.UrlManagerImpl
import com.example.passionDaily.login.manager.UserConsentManager
import com.example.passionDaily.login.manager.UserConsentManagerImpl
import com.example.passionDaily.login.manager.UserProfileManager
import com.example.passionDaily.login.manager.UserProfileManagerImpl
import com.example.passionDaily.login.stateholder.ConsentStateHolder
import com.example.passionDaily.login.stateholder.LoginStateHolder
import com.example.passionDaily.login.stateholder.UserProfileStateHolder
import com.example.passionDaily.notification.manager.FCMNotificationManager
import com.example.passionDaily.notification.manager.FCMNotificationManagerImpl
import com.example.passionDaily.notification.service.QuoteNotificationService
import com.example.passionDaily.notification.usecase.ScheduleDailyQuoteAlarmUseCase
import com.example.passionDaily.quote.data.local.repository.LocalQuoteRepository
import com.example.passionDaily.quote.domain.usecase.QuoteListManagementUseCase
import com.example.passionDaily.quote.domain.usecase.QuoteLoadingUseCase
import com.example.passionDaily.quote.domain.usecase.QuoteStateManagementUseCase
import com.example.passionDaily.quote.manager.QuoteLoadingManager
import com.example.passionDaily.quote.manager.QuoteLoadingManagerImpl
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quotecategory.data.local.repository.LocalQuoteCategoryRepository
import com.example.passionDaily.quotecategory.manager.QuoteCategoryManager
import com.example.passionDaily.quotecategory.manager.QuoteCategoryManagerImpl
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.settings.domain.usecase.LoadUserInfoUseCase
import com.example.passionDaily.settings.domain.usecase.ParseTimeUseCase
import com.example.passionDaily.settings.domain.usecase.SaveNotificationUseCase
import com.example.passionDaily.settings.domain.usecase.SendEmailUseCase
import com.example.passionDaily.settings.domain.usecase.UpdateNotificationUseCase
import com.example.passionDaily.settings.manager.EmailManager
import com.example.passionDaily.settings.manager.EmailManagerImpl
import com.example.passionDaily.settings.manager.NotificationManager
import com.example.passionDaily.settings.manager.NotificationManagerImpl
import com.example.passionDaily.settings.manager.UserSettingsManager
import com.example.passionDaily.settings.manager.UserSettingsManagerImpl
import com.example.passionDaily.toast.manager.ToastManager
import com.example.passionDaily.toast.manager.ToastManagerImpl
import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
        saveFavoritesToRemoteUseCase: SaveFavoritesToRemoteUseCase,
        getRequiredDataUseCase: GetRequiredDataUseCase
    ): FavoritesSavingManager = FavoritesSavingManagerImpl(
        saveFavoritesToLocalUseCase, saveFavoritesToRemoteUseCase, getRequiredDataUseCase
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
    fun provideUserSettingsManager(
        remoteUserRepository: RemoteUserRepository,
        localUserRepository: LocalUserRepository,
        localFavoriteRepository: LocalFavoriteRepository,
        localQuoteRepository: LocalQuoteRepository,
        localQuoteCategoryRepository: LocalQuoteCategoryRepository,
        loadUserInfoUseCase: LoadUserInfoUseCase
    ): UserSettingsManager = UserSettingsManagerImpl(
        remoteUserRepository,
        localUserRepository,
        localFavoriteRepository,
        localQuoteRepository,
        localQuoteCategoryRepository,
        loadUserInfoUseCase
    )

    @Provides
    @Singleton
    fun provideAuthenticationManager(
        getGoogleCredentialUseCase: GetGoogleCredentialUseCase,
        getFirebaseUserUseCase: GetFirebaseUserUseCase,
        loginStateHolder: LoginStateHolder,
    ): AuthenticationManager = AuthenticationManagerImpl(
        getGoogleCredentialUseCase, getFirebaseUserUseCase, loginStateHolder
    )

    @Provides
    @Singleton
    fun provideFCMNotificationManager(
        fcmService: QuoteNotificationService,
       context: Context,
    ): FCMNotificationManager = FCMNotificationManagerImpl(
        fcmService, context
    )

    @Provides
    @Singleton
    fun provideNotificationManager(
        updateNotificationUseCase: UpdateNotificationUseCase,
        scheduleAlarmUseCase: ScheduleDailyQuoteAlarmUseCase,
        parseTimeUseCase: ParseTimeUseCase,
        saveNotificationUseCase: SaveNotificationUseCase
    ): NotificationManager = NotificationManagerImpl(
        updateNotificationUseCase, scheduleAlarmUseCase, parseTimeUseCase, saveNotificationUseCase
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
    fun provideUserProfileManager(
        userProfileStateHolder: UserProfileStateHolder,
        createInitialProfileUseCase: CreateInitialProfileUseCase,
        saveUserProfileUseCase: SaveUserProfileUseCase,
        manageJsonUseCase: ManageJsonUseCase
    ): UserProfileManager = UserProfileManagerImpl(
        userProfileStateHolder,
        createInitialProfileUseCase,
        saveUserProfileUseCase,
        manageJsonUseCase
    )

    @Provides
    @Singleton
    fun provideUserConsentManager(
        consentStateHolder: ConsentStateHolder
    ): UserConsentManager = UserConsentManagerImpl(
        consentStateHolder
    )

    @Provides
    @Singleton
    fun provideUrlManager(
    ): UrlManager = UrlManagerImpl()

    @Provides
    @Singleton
    fun provideQuoteCategoryManager(
        quoteStateHolder: QuoteStateHolder
    ): QuoteCategoryManager = QuoteCategoryManagerImpl(quoteStateHolder)
}