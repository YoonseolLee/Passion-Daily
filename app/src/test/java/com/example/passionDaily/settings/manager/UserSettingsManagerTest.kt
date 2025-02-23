package com.example.passionDaily.settings.manager

import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
import com.example.passionDaily.quote.data.local.repository.LocalQuoteRepository
import com.example.passionDaily.quotecategory.data.local.repository.LocalQuoteCategoryRepository
import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import com.example.passionDaily.util.MainCoroutineRule
import io.mockk.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test

class UserSettingsManagerTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val remoteUserRepository = mockk<RemoteUserRepository>()
    private val localUserRepository = mockk<LocalUserRepository>()
    private val localFavoriteRepository = mockk<LocalFavoriteRepository>()
    private val loadUserInfoUseCase = mockk<LoadUserInfoUseCase>()

    private val userSettingsManager = UserSettingsManagerImpl(
        remoteUserRepository,
        localUserRepository,
        localFavoriteRepository,
        loadUserInfoUseCase
    )

    @Test
    fun `사용자_설정을_로드하면_LoadUserInfoUseCase에_위임되어야_한다`() = mainCoroutineRule.runTest {
        // Given
        val userId = "testUserId"
        val expectedNotificationEnabled = true
        val expectedNotificationTime = "09:00"
        var capturedEnabled: Boolean? = null
        var capturedTime: String? = null

        coEvery {
            loadUserInfoUseCase.loadUserSettings(
                userId,
                any()
            )
        } coAnswers { call ->
            val callback = call.invocation.args[1] as suspend (Boolean, String?) -> Unit
            callback(expectedNotificationEnabled, expectedNotificationTime)
        }

        // When
        userSettingsManager.loadUserSettings(userId) { enabled, time ->
            capturedEnabled = enabled
            capturedTime = time
        }

        // Then
        assertThat(capturedEnabled).isEqualTo(expectedNotificationEnabled)
        assertThat(capturedTime).isEqualTo(expectedNotificationTime)
        coVerify(exactly = 1) { loadUserInfoUseCase.loadUserSettings(userId, any()) }
    }

    @Test
    fun `사용자_데이터_삭제시_원격과_로컬_저장소_모두에서_삭제되어야_한다`() = mainCoroutineRule.runTest {
        // Given
        val userId = "testUserId"
        coEvery { remoteUserRepository.deleteUserDataFromFirestore(userId) } just Runs
        coEvery { remoteUserRepository.deleteFavoritesFromFirestore(userId) } just Runs
        coEvery { localFavoriteRepository.deleteAllFavoritesByUserId(userId) } just Runs
        coEvery { localUserRepository.deleteUser(userId) } just Runs

        // When
        userSettingsManager.deleteUserData(userId)

        // Then
        coVerifySequence {
            remoteUserRepository.deleteFavoritesFromFirestore(userId)
            remoteUserRepository.deleteUserDataFromFirestore(userId)
            localFavoriteRepository.deleteAllFavoritesByUserId(userId)
            localUserRepository.deleteUser(userId)
        }
    }
}