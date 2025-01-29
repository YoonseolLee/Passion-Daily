package com.example.passionDaily.di

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.work.WorkManager
import com.example.passionDaily.manager.ImageShareManager
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quote.stateholder.QuoteStateHolderImpl
import com.example.passionDaily.util.TimeUtil
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
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
    fun provideImageShareManager(@ApplicationContext context: Context): ImageShareManager {
        return ImageShareManager(context)
    }

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
    fun provideCoroutineExceptionHandler(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, exception ->
            Log.e("AppModule", "Unhandled coroutine exception: ${exception.message}", exception)
        }
    }

    @Provides
    fun provideDefaultDispatcher(): CoroutineDispatcher {
        return Dispatchers.Default
    }
}
