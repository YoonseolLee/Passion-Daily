package com.example.passionDaily.constants

object ViewModelConstants {
    object Quote {
        const val PAGE_SIZE = 10
        const val KEY_QUOTE_INDEX = "key_quote_index"
        const val STATE_SUBSCRIPTION_TIMEOUT_MS = 5000L
        const val DEFAULT_INDEX = 0
        const val DEFAULT_TIMEOUT_MS = 10000L
        const val MAX_RETRIES = 3
        const val INITIAL_RETRY_COUNT = 0
    }

    object Favorites {
        const val STATE_SUBSCRIPTION_TIMEOUT_MS = 5000L
        const val KEY_FAVORITE_INDEX = "favorite_quote_index"
        const val DEFAULT_INDEX = 0
        const val EMPTY_SIZE = 0
    }
}
