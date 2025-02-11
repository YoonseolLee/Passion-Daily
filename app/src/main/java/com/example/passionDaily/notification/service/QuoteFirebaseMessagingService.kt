package com.example.passionDaily.notification.service

import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.passionDaily.MainActivity
import com.example.passionDaily.PassionDailyApp
import com.example.passionDaily.R
import com.example.passionDaily.constants.AppConstants.PassionDaily.CHANNEL_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class QuoteFirebaseMessagingService : FirebaseMessagingService() {
    private val db = FirebaseFirestore.getInstance()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        updateTokenInFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        handleIncomingMessage(remoteMessage)
    }

    private fun updateTokenInFirestore(token: String) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .update("fcmToken", token)
        }
    }

    private fun handleIncomingMessage(remoteMessage: RemoteMessage) {
        Log.d("FCMService", "Message received from: ${remoteMessage.from}")

        val category = remoteMessage.data["category"] ?: return
        val quoteId = remoteMessage.data["quoteId"] ?: return

        val pendingIntent = createPendingIntent(category, quoteId)
        displayNotification(remoteMessage, pendingIntent)
    }

    private fun createPendingIntent(category: String, quoteId: String): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            data = Uri.parse("passiondaily://quote/$category/$quoteId")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            // 카테고리와 ID를 추가 데이터로도 포함
            putExtra("category", category)
            putExtra("quoteId", quoteId)
        }

        return PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun displayNotification(remoteMessage: RemoteMessage, pendingIntent: PendingIntent) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notificationBuilder.build()
        )
    }
}
