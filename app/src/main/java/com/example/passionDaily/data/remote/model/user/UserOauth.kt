package com.example.passionDaily.data.remote.model.user

import com.google.firebase.Timestamp

data class UserOauth(
    val provider: String = "", // oauth_provider
    val accessToken: String = "",
    val refreshToken: String = "",
    val expiresAt: Timestamp = Timestamp.now()
)