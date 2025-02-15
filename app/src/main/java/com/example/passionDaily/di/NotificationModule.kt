package com.example.passionDaily.di

import android.content.Context
import com.example.passionDaily.notification.service.QuoteNotificationService
import com.example.passionDaily.resources.StringProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return FirebaseRemoteConfig.getInstance()
    }

    @Provides
    @Singleton
    fun provideQuoteNotificationService(
        remoteConfig: FirebaseRemoteConfig,
        db: FirebaseFirestore,
        stringProvider: StringProvider,
        context: Context,
    ): QuoteNotificationService {
        return QuoteNotificationService(remoteConfig, db, stringProvider, context)
    }
}