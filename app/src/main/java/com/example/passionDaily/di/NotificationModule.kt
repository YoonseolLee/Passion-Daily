package com.example.passionDaily.di

import com.example.passionDaily.manager.notification.QuoteNotificationService
import com.google.firebase.firestore.FirebaseFirestore
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
    fun provideQuoteNotificationService(db: FirebaseFirestore): QuoteNotificationService {
        return QuoteNotificationService(db)
    }
}