package com.example.passionDaily.notification.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.passionDaily.R
import com.example.passionDaily.quote.data.local.model.DailyQuoteDTO
import com.example.passionDaily.notification.data.repository.remote.UserNotificationRepository
import com.example.passionDaily.notification.manager.FCMNotificationManager
import com.example.passionDaily.notification.service.QuoteNotificationService
import com.example.passionDaily.resources.StringProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@HiltWorker
class QuoteNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val fcmService: QuoteNotificationService,
    private val fcmManager: FCMNotificationManager,
    private val userRepository: UserNotificationRepository,
    private val stringProvider: StringProvider
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val currentTime = getCurrentTime()
            val todayQuote = getTodayQuote()

            if (todayQuote != null) {
                val users = getTargetUsers(currentTime)
                sendNotifications(todayQuote, users.documents)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun getCurrentTime(): String {
        return LocalTime.now().format(DateTimeFormatter.ofPattern(stringProvider.getString(R.string.hour_pattern)))
    }

    private suspend fun getTodayQuote(): DailyQuoteDTO? {
        return fcmService.getQuoteForToday()
    }

    private suspend fun getTargetUsers(currentTime: String): QuerySnapshot {
        return userRepository.getTargetUsers(currentTime)
    }

    private suspend fun sendNotifications(quote: DailyQuoteDTO, users: List<DocumentSnapshot>) {
        fcmManager.sendQuoteNotification(quote, users)
    }
}
