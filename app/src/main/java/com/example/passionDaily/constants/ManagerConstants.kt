package com.example.passionDaily.constants

object ManagerConstants {
    object QuoteLoading {
        const val TAG = "QuoteLoadingManager"
        const val PAGE_SIZE = 20
    }

    object UserConsent {
        const val TAG = "UserConsentManager"
    }

    object FCMNotification {
        const val TAG = "FCMNotificationManager"
//        const val FCM_URL =
//            "https://fcm.googleapis.com/v1/projects/passion-daily-d8b51/messages:send"
    }

    object DailyQuoteAlarmReceive {
        const val TAG = "DailyQuoteAlarmReceiver"
        const val ALARM_REQUEST_CODE = 100
    }
}