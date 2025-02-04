package com.example.passionDaily

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import com.example.passionDaily.notification.usecase.ScheduleDailyQuoteAlarmUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import androidx.work.Configuration

@HiltAndroidApp
class PassionDailyApp : Application(), Configuration.Provider {
    @Inject
    lateinit var alarmScheduler: ScheduleDailyQuoteAlarmUseCase
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        Log.d("PassionDailyApp", "Application onCreate called")

        // 알림 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default"
            val channelName = "기본 알림"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channel = android.app.NotificationChannel(
                channelId,
                channelName,
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "매일 명언 알림"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d("PassionDailyApp", "Notification channel created")
        }

        setupAlarmForExistingUsers()
    }

    private fun setupAlarmForExistingUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("PassionDailyApp", "Setting up alarms for existing users")

                // 먼저 모든 기존 알람 취소
                alarmScheduler.cancelExistingAlarm()

                val users = FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("notificationEnabled", true)
                    .get()
                    .await()

                Log.d("PassionDailyApp", "Found ${users.size()} users with notifications enabled")

                users.documents.forEach { user ->
                    user.getString("notificationTime")?.let { timeString ->
                        Log.d("PassionDailyApp", "Setting up alarm for time: $timeString")
                        val (hour, minute) = timeString.split(":").map { it.toInt() }
                        alarmScheduler.scheduleNotification(hour, minute)
                    }
                }
            } catch (e: Exception) {
                Log.e("PassionDailyApp", "Error setting up notifications", e)
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "default"
        const val CHANNEL_NAME = "기본 알림"
    }
}


