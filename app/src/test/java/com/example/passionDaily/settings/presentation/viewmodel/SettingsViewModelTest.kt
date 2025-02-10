package com.example.passionDaily.settings.presentation.viewmodel

import android.util.Log
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.toast.manager.ToastManager
import com.example.passionDaily.util.MainCoroutineRule
import com.google.firebase.auth.FirebaseUser
import android.content.Intent
import com.example.passionDaily.settings.manager.UserSettingsManager
import com.example.passionDaily.notification.usecase.ScheduleDailyQuoteAlarmUseCase
import com.example.passionDaily.settings.manager.EmailManager
import com.example.passionDaily.settings.manager.NotificationManager
import com.example.passionDaily.settings.stateholder.SettingsStateHolder
import com.google.android.gms.tasks.Task
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.LocalTime
import io.mockk.*
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.net.URISyntaxException

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class SettingsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val userSettingsManager = mockk<UserSettingsManager>()
    private val scheduleAlarmUseCase = mockk<ScheduleDailyQuoteAlarmUseCase>()
    private val authManager = mockk<AuthenticationManager>()
    private val notificationManager = mockk<NotificationManager>()
    private val authStateHolder = mockk<AuthStateHolder>()
    private val toastManager = mockk<ToastManager>()
    private val emailManager = mockk<EmailManager>()
    private val settingsStateHolder = mockk<SettingsStateHolder>()

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        mockkStatic(Log::class)
        mockkStatic(Firebase::class)
        mockkStatic(FirebaseAuth::class)

        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0

        // StateFlow 모킹
        every { settingsStateHolder.notificationEnabled } returns MutableStateFlow(false)
        every { settingsStateHolder.notificationTime } returns MutableStateFlow(LocalTime.of(8, 0))
        every { settingsStateHolder.showWithdrawalDialog } returns MutableStateFlow(false)
        every { settingsStateHolder.currentUser } returns MutableStateFlow(null)
        every { settingsStateHolder.isLoading } returns MutableStateFlow(false)

        coEvery { settingsStateHolder.updateIsLoading(any()) } just Runs
        coEvery { settingsStateHolder.updateCurrentUser(any()) } just Runs
        coEvery { settingsStateHolder.updateNotificationEnabled(any()) } just Runs
        coEvery { settingsStateHolder.updateNotificationTime(any()) } just Runs
        coEvery { settingsStateHolder.updateShowWithdrawalDialog(any()) } just Runs

        // Firebase Auth 모킹
        val mockAuth = mockk<FirebaseAuth>()
        every { Firebase.auth } returns mockAuth
        every { mockAuth.currentUser } returns null

        viewModel = SettingsViewModel(
            userSettingsManager,
            scheduleAlarmUseCase,
            authManager,
            notificationManager,
            authStateHolder,
            toastManager,
            emailManager,
            settingsStateHolder
        )
    }

    @Test
    fun `알림_설정_업데이트시_설정이_저장되고_알람이_스케줄링된다`() = mainCoroutineRule.runTest {
        // Given
        val mockUser = mockk<FirebaseUser> {
            every { uid } returns "test_uid"
        }
        every { Firebase.auth.currentUser } returns mockUser

        val currentTime = LocalTime.of(8, 0)
        every { settingsStateHolder.notificationTime.value } returns currentTime

        coEvery { notificationManager.updateNotificationSettings(any(), true) } just Runs
        every { notificationManager.scheduleNotification(any(), any()) } just Runs

        // When
        viewModel.updateNotificationSettings(true)
        advanceUntilIdle()

        // Then
        coVerify {
            notificationManager.updateNotificationSettings("test_uid", true)
            notificationManager.scheduleNotification(8, 0)  // 8시로 수정
            settingsStateHolder.updateNotificationEnabled(true)
        }
    }

    @Test
    fun `알림_비활성화시_기존_알람이_취소된다`() = mainCoroutineRule.runTest {
        // Given
        val mockUser = mockk<FirebaseUser> {
            every { uid } returns "test_uid"
        }
        every { Firebase.auth.currentUser } returns mockUser

        coEvery { notificationManager.updateNotificationSettings(any(), false) } just Runs
        every { notificationManager.cancelExistingAlarm() } just Runs

        // When
        viewModel.updateNotificationSettings(false)

        // Then
        verify { notificationManager.cancelExistingAlarm() }
        coVerify { settingsStateHolder.updateNotificationEnabled(false) }
    }

    @Test
    fun `사용자_설정_로드_성공시_StateHolder가_업데이트된다`() = mainCoroutineRule.runTest {
        // Given
        val mockUser = mockk<FirebaseUser>()
        every { mockUser.uid } returns "test_uid"
        every { Firebase.auth.currentUser } returns mockUser

        coEvery {
            userSettingsManager.loadUserSettings(
                userId = "test_uid",
                onSettingsLoaded = any()
            )
        } coAnswers {
            secondArg<suspend (Boolean, String?) -> Unit>().invoke(true, "09:00")
        }

        val expectedTime = LocalTime.of(9, 0)
        every { notificationManager.parseTime("09:00") } returns expectedTime

        // When
        viewModel.loadUserSettings()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) {
            settingsStateHolder.updateNotificationEnabled(true)
            settingsStateHolder.updateNotificationTime(expectedTime)
        }
    }

    @Test
    fun `이메일_인텐트_생성_성공시_인텐트가_반환된다`() {
        // Given
        val mockIntent = mockk<Intent>()
        every { emailManager.createEmailIntent() } returns mockIntent

        // When
        val result = viewModel.createEmailIntent()

        // Then
        assertThat(result).isEqualTo(mockIntent)
    }

    @Test
    fun `이메일_인텐트_생성_실패시_null이_반환되고_에러_토스트가_표시된다`() {
        // Given
        every { emailManager.createEmailIntent() } throws Exception("Error")
        coEvery { toastManager.showGeneralErrorToast() } just Runs

        // When
        val result = viewModel.createEmailIntent()

        // Then
        assertThat(result).isNull()
        coVerify { toastManager.showGeneralErrorToast() }
    }

    @Test
    fun `알림_설정_업데이트_실패시_에러_토스트가_표시된다`() = mainCoroutineRule.runTest {
        // Given
        val mockUser = mockk<FirebaseUser> {
            every { uid } returns "test_uid"
        }
        every { Firebase.auth.currentUser } returns mockUser

        coEvery {
            notificationManager.updateNotificationSettings(any(), any())
        } throws Exception("Network error")

        coEvery { toastManager.showGeneralErrorToast() } just Runs

        // When
        viewModel.updateNotificationSettings(true)
        advanceUntilIdle()

        // Then
        coVerify { toastManager.showGeneralErrorToast() }
        coVerify(exactly = 0) { notificationManager.scheduleNotification(any(), any()) }
    }

    @Test
    fun `사용자_설정_로드_실패시_에러_토스트가_표시된다`() = mainCoroutineRule.runTest {
        // Given
        val mockUser = mockk<FirebaseUser> {
            every { uid } returns "test_uid"
        }
        every { Firebase.auth.currentUser } returns mockUser

        coEvery {
            userSettingsManager.loadUserSettings(any(), any())
        } throws Exception("Failed to load settings")

        coEvery { toastManager.showGeneralErrorToast() } just Runs

        // When
        viewModel.loadUserSettings()
        advanceUntilIdle()

        // Then
        coVerify { toastManager.showGeneralErrorToast() }
        coVerify(exactly = 0) { settingsStateHolder.updateNotificationEnabled(any()) }
        coVerify(exactly = 0) { settingsStateHolder.updateNotificationTime(any()) }
    }

    @Test
    fun `로그인되지_않은_상태에서_회원탈퇴_시도시_에러_토스트가_표시된다`() = mainCoroutineRule.runTest {
        // Given
        every { Firebase.auth.currentUser } returns null
        coEvery { toastManager.showLogInRequiredErrorToast() } just Runs

        // When
        viewModel.withdrawUser({}, {})
        advanceUntilIdle()

        // Then
        coVerify { toastManager.showLogInRequiredErrorToast() }
        coVerify(exactly = 0) { userSettingsManager.deleteUserData(any()) }
    }

    @Test
    fun `이메일_인텐트_생성_실패시_에러_토스트가_표시된다`() = mainCoroutineRule.runTest {
        // Given
        every { emailManager.createEmailIntent() } throws URISyntaxException("invalid uri", "reason")
        coEvery { toastManager.showURISyntaxException() } just Runs

        // When
        val result = viewModel.createEmailIntent()

        // Then
        assertNull(result)
        coVerify { toastManager.showURISyntaxException() }
    }
}