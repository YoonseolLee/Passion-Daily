package com.example.passionDaily.login.manager

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UrlManagerTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var urlManager: UrlManager
    private lateinit var mockContext: Context
    private val intentSlot = slot<Intent>()

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        urlManager = UrlManagerImpl()
    }

    @Test
    fun `URL 실행시 Intent가 올바르게 생성된다`() = mainCoroutineRule.runTest {
        // Given
        val testUrl = "https://www.example.com"
        every { mockContext.startActivity(capture(intentSlot)) } just runs

        // When
        urlManager.openUrl(mockContext, testUrl)

        // Then
        verify { mockContext.startActivity(any()) }

        val capturedIntent = intentSlot.captured
        assertThat(capturedIntent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(capturedIntent.dataString).isEqualTo(testUrl)
        assertThat(capturedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
            .isEqualTo(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @Test
    fun `잘못된 URL 실행시에도 Intent는 생성된다`() = mainCoroutineRule.runTest {
        // Given
        val invalidUrl = "not a valid url"
        every { mockContext.startActivity(capture(intentSlot)) } just runs

        // When
        urlManager.openUrl(mockContext, invalidUrl)

        // Then
        verify { mockContext.startActivity(any()) }

        val capturedIntent = intentSlot.captured
        assertThat(capturedIntent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(capturedIntent.dataString).isEqualTo(invalidUrl)
    }

    @Test
    fun `Activity 실행 실패시 예외가 발생한다`() = mainCoroutineRule.runTest {
        // Given
        val testUrl = "https://www.example.com"
        every { mockContext.startActivity(any()) } throws ActivityNotFoundException()

        // When & Then
        try {
            urlManager.openUrl(mockContext, testUrl)
            error("Expected ActivityNotFoundException")
        } catch (e: ActivityNotFoundException) {
            assertThat(e).isInstanceOf(ActivityNotFoundException::class.java)
        }
    }
}


