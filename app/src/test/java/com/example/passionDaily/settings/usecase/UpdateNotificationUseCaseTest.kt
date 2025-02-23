package com.example.passionDaily.settings.usecase

import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import com.example.passionDaily.util.MainCoroutineRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class UpdateNotificationUseCaseTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val remoteUserRepository = mockk<RemoteUserRepository>()
    private val localUserRepository = mockk<LocalUserRepository>()
    private lateinit var updateNotificationUseCase: UpdateNotificationUseCase

    @Before
    fun setup() {
        updateNotificationUseCase = UpdateNotificationUseCase(
            remoteUserRepository,
            localUserRepository
        )
    }

    @Test
    fun 알림_설정_업데이트시_저장소에_저장된다() = mainCoroutineRule.runTest {
        // given
        coEvery { remoteUserRepository.updateNotificationSettingsToFirestore(any(), any()) } just Runs
        coEvery { localUserRepository.updateNotificationSettingsToRoom(any(), any()) } just Runs

        // when
        updateNotificationUseCase.updateNotificationSettingsToFirestore("test_id", true)
        updateNotificationUseCase.updateNotificationSettingsToRoom("test_id", true)

        // then
        coVerifyOrder {
            remoteUserRepository.updateNotificationSettingsToFirestore("test_id", true)
            localUserRepository.updateNotificationSettingsToRoom("test_id", true)
        }
    }
}