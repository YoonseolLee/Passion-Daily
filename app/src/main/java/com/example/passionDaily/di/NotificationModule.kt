package com.example.passionDaily.di

import android.content.Context
import com.example.passionDaily.manager.notification.QuoteNotificationService
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
        context: Context
    ): QuoteNotificationService {
        return QuoteNotificationService(remoteConfig, db, context)
    }
}