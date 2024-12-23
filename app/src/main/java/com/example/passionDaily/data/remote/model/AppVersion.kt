package com.example.passionDaily.data.remote.model

import com.google.firebase.Timestamp

data class AppVersion(
    val version: String,
    val changeLog: String,
    val createdAt: Timestamp,
    val isForceUpdate: Boolean,
    val releaseDate: Timestamp
)