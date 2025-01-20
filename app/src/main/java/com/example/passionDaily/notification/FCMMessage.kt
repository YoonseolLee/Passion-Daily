package com.example.passionDaily.notification

data class FCMMessage(
    val title: String = "오늘의 명언",
    val body: String,
)