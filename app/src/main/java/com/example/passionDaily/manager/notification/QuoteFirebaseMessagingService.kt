package com.example.passionDaily.manager.notification

import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.passionDaily.MainActivity
import com.example.passionDaily.PassionDailyApp
import com.example.passionDaily.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class QuoteFirebaseMessagingService : FirebaseMessagingService() {
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

        // FCM 메시지에서 카테고리와 인용구 ID 추출
        val category = remoteMessage.data["category"] ?: return
        val quoteId = remoteMessage.data["quoteId"] ?: return

        // 딥링크를 포함한 Intent 생성
        val intent = Intent(this, MainActivity::class.java).apply {
            data = Uri.parse("passiondaily://quote/$category/$quoteId")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 생성
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager

        val notificationBuilder = NotificationCompat.Builder(this, PassionDailyApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.add_to_favorites_icon)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notificationBuilder.build()
        )

        Log.d("FCMService", "Notification displayed with deeplink: passiondaily://quote/$category/$quoteId")
    }
}