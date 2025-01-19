package com.example.passionDaily.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // FCM 메시지 처리 로직
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // 새로운 토큰 처리 로직
    }
}