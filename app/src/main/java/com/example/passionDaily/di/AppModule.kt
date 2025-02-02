package com.example.passionDaily.di

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.work.WorkManager
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.login.stateholder.AuthStateHolderImpl
import com.example.passionDaily.login.stateholder.LoginStateHolder
import com.example.passionDaily.login.stateholder.LoginStateHolderImpl
import com.example.passionDaily.login.stateholder.UserProfileStateHolder
import com.example.passionDaily.login.stateholder.UserProfileStateHolderImpl
import com.example.passionDaily.manager.QuoteCategoryManager
import com.example.passionDaily.quote.domain.usecase.ImageShareUseCase
import com.example.passionDaily.quote.domain.usecase.IncrementShareCountUseCase
import com.example.passionDaily.quote.domain.usecase.QuoteListManagementUseCase
import com.example.passionDaily.quote.domain.usecase.QuoteLoadingUseCase
import com.example.passionDaily.quote.domain.usecase.QuoteStateManagementUseCase
import com.example.passionDaily.quote.manager.QuoteLoadingManager
import com.example.passionDaily.quote.manager.QuoteLoadingManagerImpl
import com.example.passionDaily.quote.manager.ShareQuoteManager
import com.example.passionDaily.quote.manager.ShareQuoteManagerImpl
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quote.stateholder.QuoteStateHolderImpl
import com.example.passionDaily.util.TimeUtil
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager {
        return CredentialManager.create(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideTimeUtil(): TimeUtil = TimeUtil

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideQuoteStateHolder(): QuoteStateHolder {
        return QuoteStateHolderImpl()
    }

    @Provides
    @Singleton
    fun provideQuoteCategoryManager(
        quoteStateHolder: QuoteStateHolder
    ) : QuoteCategoryManager {
        return QuoteCategoryManager(quoteStateHolder)
    }

    @Provides
    @Singleton
    fun provideQuoteLoadingManager(
        managementUseCase: QuoteStateManagementUseCase,
        loadingUseCase: QuoteLoadingUseCase,
        listManagementUseCase: QuoteListManagementUseCase
    ): QuoteLoadingManager {
        return QuoteLoadingManagerImpl(managementUseCase, loadingUseCase, listManagementUseCase)
    }

    @Provides
    @Singleton
    fun provideShareQuoteManager(
        imageShareUseCase: ImageShareUseCase,
        incrementShareCountUseCase: IncrementShareCountUseCase
    ): ShareQuoteManager {
        return ShareQuoteManagerImpl(imageShareUseCase, incrementShareCountUseCase)
    }

    @Provides
    @Singleton
    fun provideAuthStateHolder(): AuthStateHolder {
        return AuthStateHolderImpl()
    }

    @Provides
    @Singleton
    fun provideLoginStateHolder(): LoginStateHolder {
        return LoginStateHolderImpl()
    }

    @Provides
    @Singleton
    fun provideUserProfileStateHolder(): UserProfileStateHolder {
        return UserProfileStateHolderImpl()
    }
}
