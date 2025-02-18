package com.example.passionDaily.notification.manager

import app.cash.turbine.test
import com.example.passionDaily.notification.service.QuoteNotificationService
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.DocumentSnapshot
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.passionDaily.quote.data.local.model.DailyQuoteDTO
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.util.MainCoroutineRule
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.After
import java.io.InputStream
import java.util.Calendar

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class FCMNotificationManagerImplTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var fcmService: QuoteNotificationService
    private lateinit var stringProvider: StringProvider
    private lateinit var context: Context
    private lateinit var manager: FCMNotificationManagerImpl
    private lateinit var mockInputStream: InputStream
    private lateinit var mockAssetManager: android.content.res.AssetManager
    private lateinit var mockCredentials: GoogleCredentials

    @Before
    fun setUp() {
        fcmService = mockk()
        stringProvider = mockk()
        context = mockk()
        mockInputStream = mockk()
        mockAssetManager = mockk()
        mockCredentials = mockk()

        every { context.assets } returns mockAssetManager
        every { mockAssetManager.open(any()) } returns mockInputStream
        every { stringProvider.getString(any()) } returns "test_string"

        mockkStatic(GoogleCredentials::class)
        every { GoogleCredentials.fromStream(any()) } returns mockCredentials
        every { mockCredentials.createScoped(any<List<String>>()) } returns mockCredentials
        every { mockCredentials.refreshAccessToken() } returns mockk {
            every { tokenValue } returns "test_token"
        }

        every { fcmService.monthlyQuotes } returns MutableStateFlow(
            listOf(Triple("category", "quoteId", 0))
        )

        manager = FCMNotificationManagerImpl(fcmService, context, stringProvider)
    }

    @Test
    fun `FCM_알림_발송_전_월간_명언_구독이_성공적으로_초기화되는_경우`() = mainCoroutineRule.runTest {
        // given
        val quotes = listOf(
            Triple("category1", "quote1", 0),
            Triple("category2", "quote2", 1)
        )
        val quotesFlow = MutableStateFlow(quotes)

        // when
        every { fcmService.monthlyQuotes } returns quotesFlow

        // then
        quotesFlow.test {
            val emission = awaitItem()
            assertThat(emission).hasSize(2)
            assertThat(emission).containsExactlyElementsIn(quotes)
        }
    }
}

