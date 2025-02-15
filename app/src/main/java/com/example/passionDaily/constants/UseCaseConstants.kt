package com.example.passionDaily.constants

object UseCaseConstants {
    object QuoteLoading {
        const val TAG = "QuoteLoadingUseCase"
    }

    object UserProfileConstants {
        const val ROLE_USER = "USER"
        const val DEFAULT_NOTIFICATION_TIME = "12:00"
        const val DEFAULT_NOTIFICATION_ENABLED = true
    }

    object ScheduleDailyQuoteAlarm {
        const val TAG = "ScheduleDailyQuoteAlarm"
        const val ALARM_REQUEST_CODE = 100
    }
}