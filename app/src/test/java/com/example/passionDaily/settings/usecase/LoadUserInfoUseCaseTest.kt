package com.example.passionDaily.settings.usecase

import com.example.passionDaily.user.data.local.entity.UserEntity
import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class LoadUserInfoUseCaseTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val localUserRepository = mockk<LocalUserRepository>()
    private lateinit var loadUserInfoUseCase: LoadUserInfoUseCase

    @Before
    fun setup() {
        loadUserInfoUseCase = LoadUserInfoUseCase(localUserRepository)
    }

    @Test
    fun 사용자_정보_로드_성공시_콜백이_호출된다() = mainCoroutineRule.runTest {
        // given
        val testUser = UserEntity(
            userId = "test_id",
            notificationEnabled = true,
            notificationTime = "09:00",
            email = "example@naver.com",
            lastSyncDate = 2002
        )
        coEvery { localUserRepository.getUserById("test_id") } returns testUser

        var resultEnabled: Boolean? = null
        var resultTime: String? = null

        // when
        loadUserInfoUseCase.loadUserSettings("test_id") { enabled, time ->
            resultEnabled = enabled
            resultTime = time
        }

        // then
        assertThat(resultEnabled).isTrue()
        assertThat(resultTime).isEqualTo("09:00")
    }

    @Test
    fun 사용자_정보가_없을시_콜백이_호출되지_않는다() = mainCoroutineRule.runTest {
        // given
        coEvery { localUserRepository.getUserById("test_id") } returns null
        var callbackCalled = false

        // when
        loadUserInfoUseCase.loadUserSettings("test_id") { _, _ ->
            callbackCalled = true
        }

        // then
        assertThat(callbackCalled).isFalse()
    }
}