package com.example.passionDaily.manager.notification

import android.content.Context
import android.util.Log
import com.example.passionDaily.data.constants.QuoteConfig
import com.example.passionDaily.data.model.DailyQuote
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
            }
            else {
                minimumFetchIntervalInSeconds = 43200
            }
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    private fun setDefaultQuoteData() {
        val defaultQuotes = readAssetFile("default_quotes.json")
        remoteConfig.setDefaultsAsync(mapOf("monthly_quotes" to defaultQuotes))
    }

    private fun initializeQuoteData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                fetchQuoteDataFromRemoteConfig()
            } catch (e: Exception) {
                Log.e("QuoteNotificationService", "Failed to load remote quotes, using default values", e)
                useDefaultQuoteData()
            }
        }
    }

    private suspend fun fetchQuoteDataFromRemoteConfig() {
        remoteConfig.fetchAndActivate().await()
        val quotesJson = remoteConfig.getString("monthly_quotes")

        if (quotesJson.isNotEmpty()) {
            processQuoteData(quotesJson, "Remote Config")
        } else {
            Log.w("QuoteNotificationService", "Remote Config returned empty quotes, using default values")
            useDefaultQuoteData()
        }
    }

    private suspend fun useDefaultQuoteData() {
        try {
            val defaultQuotes = readAssetFile("default_quotes.json")
            processQuoteData(defaultQuotes, "Default")
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
            val quoteConfigs = json.decodeFromString<List<QuoteConfig>>(jsonString)
            val quotesTriples = quoteConfigs.map { config ->
                Triple(config.category.lowercase(), config.quoteId, config.day)
            }
            _monthlyQuotes.emit(quotesTriples)
            _isInitialized.emit(true)
            Log.d("QuoteNotificationService", "Loaded ${quotesTriples.size} quotes successfully from $source")
        } catch (e: Exception) {
            Log.e("QuoteNotificationService", "Failed to process quotes from $source", e)
            throw e
        }
    }

    suspend fun getQuoteForToday(): DailyQuote? {
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

        val adjustedDay = (day - 1).coerceIn(0, quotes.size - 1)
        return quotes.find { it.third == adjustedDay }
    }

    private suspend fun fetchQuoteFromFirestore(quoteInfo: Triple<String, String, Int>): DailyQuote {
        val (category, quoteId, _) = quoteInfo
        val quoteDoc = retrieveQuoteDocument(category, quoteId)
        return parseDailyQuote(quoteDoc)
    }

    private suspend fun retrieveQuoteDocument(category: String, quoteId: String): DocumentSnapshot {
        return db.collection("categories")
            .document(category)
            .collection("quotes")
            .document(quoteId)
            .get()
            .await()
    }

    private fun parseDailyQuote(document: DocumentSnapshot): DailyQuote {
        val text = document.getString("text") ?: ""
        val person = document.getString("person") ?: ""
        Log.d("QuoteNotificationService", "Quote retrieved: $text by $person")
        return DailyQuote(text = text, person = person)
    }
}