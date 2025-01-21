package com.example.passionDaily.manager.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.passionDaily.data.repository.remote.UserNotificationRepository
import com.example.passionDaily.manager.notification.FCMNotificationManager
import com.example.passionDaily.manager.notification.QuoteNotificationService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@HiltWorker
class QuoteNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val fcmService: QuoteNotificationService,
    private val fcmManager: FCMNotificationManager,
    private val userRepository: UserNotificationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1

            val todayQuote = fcmService.getQuoteForDay(dayOfWeek)
            if (todayQuote != null) {
                val users = userRepository.getTargetUsers(currentTime)
                fcmManager.sendQuoteNotification(todayQuote, users.documents)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notifications", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "QuoteNotificationWorker"
    }
}
