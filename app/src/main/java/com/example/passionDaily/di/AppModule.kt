package com.example.passionDaily.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.example.passionDaily.R
import com.example.passionDaily.manager.ImageShareManager
import com.example.passionDaily.util.TimeUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
    fun provideImageShareManager(@ApplicationContext context: Context): ImageShareManager {
        return ImageShareManager(context)
    }
}
