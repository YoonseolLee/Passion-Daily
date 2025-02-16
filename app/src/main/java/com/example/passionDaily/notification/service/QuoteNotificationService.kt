package com.example.passionDaily.notification.service

import android.content.Context
import android.util.Log
import com.example.passionDaily.R
import com.example.passionDaily.quote.data.local.model.QuoteConfigDTO
import com.example.passionDaily.quote.data.local.model.DailyQuoteDTO
import com.example.passionDaily.resources.StringProvider
import com.google.firebase.firestore.BuildConfig
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteNotificationService @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val db: FirebaseFirestore,
    private val stringProvider: StringProvider,
    @ApplicationContext private val context: Context,
) {
    private val _monthlyQuotes = MutableStateFlow<List<Triple<String, String, Int>>>(emptyList())
    val monthlyQuotes = _monthlyQuotes.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized = _isInitialized.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    init {
        setupRemoteConfig()
        initializeQuoteData()
    }

    private fun setupRemoteConfig() {
        configureRemoteConfigSettings()
        setDefaultQuoteData()
    }

    private fun configureRemoteConfigSettings() {
        val configSettings = remoteConfigSettings {
            if (BuildConfig.DEBUG) {
                minimumFetchIntervalInSeconds = 0
            } else {
                minimumFetchIntervalInSeconds = 43200
            }
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    private fun setDefaultQuoteData() {
        val defaultQuotes = readAssetFile(stringProvider.getString(R.string.default_quotes_json))
        remoteConfig.setDefaultsAsync(mapOf(stringProvider.getString(R.string.monthly_quotes) to defaultQuotes))
    }

    private fun initializeQuoteData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                fetchQuoteDataFromRemoteConfig()
            } catch (e: Exception) {
                useDefaultQuoteData()
            }
        }
    }

    private suspend fun fetchQuoteDataFromRemoteConfig() {
        try {
            // 강제로 서버에서 최신 데이터를 가져오도록 설정
            remoteConfig.fetchAndActivate().await()

            val quotesJson =
                remoteConfig.getString(stringProvider.getString(R.string.monthly_quotes))

            if (quotesJson.isNotEmpty()) {
                processQuoteData(quotesJson, stringProvider.getString(R.string.remote_config))
            } else {
                Log.w(
                    "QuoteNotificationService",
                    "Remote Config returned empty quotes, using default values"
                )
                useDefaultQuoteData()
            }
        } catch (e: Exception) {
            Log.e("QuoteNotificationService", "Error fetching from Remote Config: ${e.message}")
            throw e
        }
    }

    private suspend fun useDefaultQuoteData() {
        try {
            val defaultQuotes =
                readAssetFile(stringProvider.getString(R.string.default_quotes_json))
            processQuoteData(defaultQuotes, stringProvider.getString(R.string.default_string))
        } catch (e: Exception) {
            Log.e("QuoteNotificationService", "Failed to load default quotes", e)
            _monthlyQuotes.emit(emptyList())
            _isInitialized.emit(true)
        }
    }

    private fun readAssetFile(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    private suspend fun processQuoteData(jsonString: String, source: String) {
        try {
            val quoteConfigDTOS = json.decodeFromString<List<QuoteConfigDTO>>(jsonString)
            Log.d(
                "QuoteNotificationService",
                "Parsed quotes from $source: ${quoteConfigDTOS.map { 
                    "${it.day}: ${it.category}/${it.quoteId}" }}"
            )

            val quotesTriples = quoteConfigDTOS.map { config ->
                Triple(config.category, config.quoteId, config.day)
            }
            _monthlyQuotes.emit(quotesTriples)
            _isInitialized.emit(true)
            Log.d(
                "QuoteNotificationService",
                "Successfully loaded ${quotesTriples.size} quotes from $source"
            )
        } catch (e: Exception) {
            Log.e("QuoteNotificationService", "Failed to process quotes from $source", e)
            throw e
        }
    }

    suspend fun getQuoteForToday(): DailyQuoteDTO? {
        return try {
            waitUntilInitializationComplete()

            val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            Log.d("QuoteNotificationService", "Fetching quote for day $today")

            val quoteInfo = findQuoteForDay(today)
            if (quoteInfo != null) {
                fetchQuoteFromFirestore(quoteInfo)
            } else {
                Log.w("QuoteNotificationService", "No quote found for day $today")
                null
            }
        } catch (e: Exception) {
            Log.e("QuoteNotificationService", "Failed to fetch quote for today", e)
            null
        }
    }

    private suspend fun waitUntilInitializationComplete() {
        withTimeout(5000) {
            if (!isInitialized.value) {
                isInitialized.first { it }
            }
        }
    }

    private fun findQuoteForDay(day: Int): Triple<String, String, Int>? {
        val quotes = monthlyQuotes.value
        if (quotes.isEmpty()) {
            Log.w("QuoteNotificationService", "No quotes available")
            return null
        }

        Log.d("QuoteNotificationService", "Finding quote for day: $day")
        Log.d(
            "QuoteNotificationService",
            "Available quotes: ${quotes.map { "day=${it.third}, category=${it.first}, quoteId=${it.second}" }}"
        )

        // 직접 day로 찾기
        return quotes.find { it.third == day }.also {
            Log.d("QuoteNotificationService", "Found quote: $it")
        }
    }

    private suspend fun fetchQuoteFromFirestore(quoteInfo: Triple<String, String, Int>): DailyQuoteDTO {
        val (category, quoteId, _) = quoteInfo
        val quoteDoc = retrieveQuoteDocument(category, quoteId)
        return parseDailyQuote(quoteDoc)
    }

    private suspend fun retrieveQuoteDocument(category: String, quoteId: String): DocumentSnapshot {
        val docRef = db.collection("categories")
            .document(category)
            .collection("quotes")
            .document(quoteId)

        val snapshot = docRef.get().await()
        if (!snapshot.exists()) {
            Log.e(
                "QuoteNotificationService",
                "Document does not exist at path: categories/$category/quotes/$quoteId"
            )
        } else {
            Log.d("QuoteNotificationService", "Document fields: ${snapshot.data}")
        }
        return snapshot
    }

    private fun parseDailyQuote(document: DocumentSnapshot): DailyQuoteDTO {
        val text = document.getString(stringProvider.getString(R.string.text)) ?: ""
        val person = document.getString(stringProvider.getString(R.string.person)) ?: ""
        return DailyQuoteDTO(text = text, person = person)
    }
}