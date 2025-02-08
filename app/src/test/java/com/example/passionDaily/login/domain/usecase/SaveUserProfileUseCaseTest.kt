package com.example.passionDaily.login.domain.usecase

import com.example.passionDaily.util.mapper.UserProfileMapper
import com.google.common.truth.Truth.assertThat
import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import com.example.passionDaily.login.stateholder.AuthStateHolder
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class SaveUserProfileUseCaseTest {

    private lateinit var saveUserProfileUseCase: SaveUserProfileUseCase
    private val userProfileMapper: UserProfileMapper = mockk()
    private val localUserRepository: LocalUserRepository = mockk(relaxed = true)
    private val remoteUserRepository: RemoteUserRepository = mockk(relaxed = true)
    private val authStateHolder: AuthStateHolder = mockk(relaxed = true)
    private val profileMap: Map<String, Any?> = mockk()
    private val userId = "testUserId"

    @Before
    fun setUp() {
        saveUserProfileUseCase = SaveUserProfileUseCase(userProfileMapper, localUserRepository, remoteUserRepository, authStateHolder)
        every { userProfileMapper.mapToUserEntity(profileMap) } returns mockk()
    }

    @Test
    fun `Room에 사용자 데이터를 저장하면 userId를 반환`() = runBlocking {
        val result = saveUserProfileUseCase.saveToRoom(profileMap, userId)
        assertThat(result).isEqualTo(userId)
    }

    @Test
    fun `Firestore에 사용자 데이터를 저장하면 userId를 반환`() = runBlocking {
        val result = saveUserProfileUseCase.saveUserToFirestore(userId, profileMap)
        assertThat(result).isEqualTo(userId)
    }

    @Test
    fun `기존 사용자의 동기화가 정상적으로 수행된다`() = runBlocking {
        saveUserProfileUseCase.syncExistingUser(userId)
        runBlocking {
            coVerify { remoteUserRepository.updateLastSyncDate(userId) }
            coVerify { remoteUserRepository.syncFirestoreUserToRoom(userId) }
            coVerify { authStateHolder.setAuthenticated(userId) }
        }
    }
}