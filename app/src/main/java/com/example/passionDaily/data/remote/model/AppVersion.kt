package com.example.passionDaily.data.remote.model

import com.google.firebase.Timestamp

data class AppVersion(
    val version: String = "",
    val releaseDate: Timestamp = Timestamp.now(),
    val isForceUpdate: Boolean = false,
    val changeLog: String = "",
    val createdDate: Timestamp = Timestamp.now()
)