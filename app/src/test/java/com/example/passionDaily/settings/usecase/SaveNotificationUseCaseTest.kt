package com.example.passionDaily.settings.usecase


import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.settings.domain.usecase.SaveNotificationUseCase
import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import com.example.passionDaily.util.MainCoroutineRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.LocalTime

@ExperimentalMultiplatform
@RunWith(JUnit4::class)
class SaveNotificationUseCaseTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val remoteUserRepository = mockk<RemoteUserRepository>()
    private val localUserRepository = mockk<LocalUserRepository>()
    private val stringProvider = mockk<StringProvider>()
    private lateinit var saveNotificationUseCase: SaveNotificationUseCase

    @Before
    fun setup() {
        saveNotificationUseCase = SaveNotificationUseCase(
            remoteUserRepository,
            localUserRepository,
            stringProvider
        )
        every { stringProvider.getString(any()) } returns "HH:mm"
    }

    @Test
    fun 알림_시간_업데이트시_저장소에_저장된다() = mainCoroutineRule.runTest {
        // given
        val time = LocalTime.of(9, 0)
        coEvery { remoteUserRepository.updateNotificationTimeToFirestore(any(), any()) } just Runs
        coEvery { localUserRepository.updateNotificationTimeToRoom(any(), any()) } just Runs

        // when
        saveNotificationUseCase.updateNotificationTimeToFirestore("test_id", time)
        saveNotificationUseCase.updateNotificationTimeToRoom("test_id", time)

        // then
        coVerifyOrder {
            remoteUserRepository.updateNotificationTimeToFirestore("test_id", "09:00")
            localUserRepository.updateNotificationTimeToRoom("test_id", "09:00")
        }
    }
}
