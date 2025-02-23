package com.example.passionDaily.settings.presentation.viewmodel

import android.content.Intent
import com.example.passionDaily.settings.manager.EmailManager
import com.example.passionDaily.toast.manager.ToastManager
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.net.URISyntaxException

class SettingsViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: SettingsViewModel
    private val toastManager: ToastManager = mockk(relaxed = true)
    private val emailManager: EmailManager = mockk()

    @Before
    fun setup() {
        viewModel = SettingsViewModel(toastManager, emailManager)
    }

    @Test
    fun `이메일 인텐트가 정상적으로 생성된다`() {
        // Given
        val mockIntent = mockk<Intent>()
        every { emailManager.createEmailIntent() } returns mockIntent

        // When
        val result = viewModel.createEmailIntent()

        // Then
        assertThat(result).isEqualTo(mockIntent)
        verify { emailManager.createEmailIntent() }
    }

    @Test
    fun `URI 문법 오류시 토스트를 표시하고 null을 반환한다`() {
        // Given
        every { emailManager.createEmailIntent() } throws URISyntaxException("", "")

        // When
        val result = viewModel.createEmailIntent()

        // Then
        assertThat(result).isNull()
        verify { toastManager.showURISyntaxException() }
    }

    @Test
    fun `네트워크 에러시 적절한 토스트를 표시한다`() {
        // Given
        every { emailManager.createEmailIntent() } throws IOException()

        // When
        val result = viewModel.createEmailIntent()

        // Then
        assertThat(result).isNull()
        verify { toastManager.showNetworkErrorToast() }
    }
}