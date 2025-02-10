package com.example.passionDaily.settings.usecase

import android.content.Intent
import android.net.Uri
import com.example.passionDaily.resources.StringProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SendEmailUseCaseTest {
    private val stringProvider = mockk<StringProvider>()
    private lateinit var sendEmailUseCase: SendEmailUseCase

    @Before
    fun setup() {
        sendEmailUseCase = SendEmailUseCase(stringProvider)
        every { stringProvider.getString(any()) } returns "mailto:test@example.com"
    }

    @Test
    fun `createEmailIntent는 올바른 이메일 인텐트를 생성한다`() {
        // When
        val intent = sendEmailUseCase.createEmailIntent()

        // Then
        assertThat(intent.action).isEqualTo(Intent.ACTION_SENDTO)
        assertThat(intent.data).isEqualTo(Uri.parse("mailto:test@example.com"))
    }
}