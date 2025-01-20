package com.example.passionDaily.notification

import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.passionDaily.PassionDailyApp
import com.example.passionDaily.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val db = FirebaseFirestore.getInstance()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .update("fcmToken", token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCMService", "Message received from: ${remoteMessage.from}")

        // 알림 생성
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager

        val notificationBuilder = NotificationCompat.Builder(this, PassionDailyApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.add_to_favorites_icon) // 앱의 알림 아이콘
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

            // 진동 패턴 설정 (선택사항)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))

            // 알림음 설정 (선택사항)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notificationBuilder.build()
        )

        Log.d("FCMService", "Notification displayed")
    }
}