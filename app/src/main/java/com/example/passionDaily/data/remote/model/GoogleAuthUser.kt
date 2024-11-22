package com.example.passionDaily.data.remote.model

data class GoogleAuthUser(
    val userId: String,
    val username: String?,
    val email: String?,
    val profilePictureUrl: String?
)