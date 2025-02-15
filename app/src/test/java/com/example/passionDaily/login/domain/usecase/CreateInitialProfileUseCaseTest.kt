package com.example.passionDaily.login.domain.usecase

import com.example.passionDaily.constants.UseCaseConstants
import com.example.passionDaily.login.domain.model.UserProfileKey
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertThrows

class CreateInitialProfileUseCaseTest {
    private lateinit var createInitialProfileUseCase: CreateInitialProfileUseCase
    private val firebaseUser: FirebaseUser = mockk()

    @Before
    fun setUp() {
        createInitialProfileUseCase = CreateInitialProfileUseCase()
        every { firebaseUser.uid } returns "testUserId"
        every { firebaseUser.email } returns "test@example.com"
    }

    @Test
    fun `유효한 FirebaseUser와 userId를 제공하면 프로필 맵이 올바르게 생성된다`() {
        val userId = "testUserId"
        val profileMap = createInitialProfileUseCase.createInitialProfile(firebaseUser, userId)

        assertThat(profileMap).isNotEmpty()
        assertThat(profileMap[UserProfileKey.ID.key]).isEqualTo(userId)
        assertThat(profileMap[UserProfileKey.EMAIL.key]).isEqualTo("test@example.com")
        assertThat(profileMap[UserProfileKey.ROLE.key]).isEqualTo(UseCaseConstants.UserProfileConstants.ROLE_USER)
    }

    @Test
    fun `userId가 비어있으면 IllegalArgumentException을 던진다`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            createInitialProfileUseCase.createInitialProfile(firebaseUser, "")
        }
        assertThat(exception).hasMessageThat().contains("User ID cannot be blank")
    }

    @Test
    fun `FirebaseUser의 UID가 비어있으면 IllegalArgumentException을 던진다`() {
        every { firebaseUser.uid } returns ""
        val exception = assertThrows(IllegalArgumentException::class.java) {
            createInitialProfileUseCase.createInitialProfile(firebaseUser, "testUserId")
        }
        assertThat(exception).hasMessageThat().contains("Firebase user ID cannot be blank")
    }

    @Test
    fun `FirebaseUser의 이메일이 null이면 IllegalArgumentException을 던진다`() {
        every { firebaseUser.email } returns null
        val exception = assertThrows(IllegalArgumentException::class.java) {
            createInitialProfileUseCase.createInitialProfile(firebaseUser, "testUserId")
        }
        assertThat(exception).hasMessageThat().contains("Firebase user email cannot be empty")
    }

    @Test
    fun `FirebaseUser의 UID와 userId가 다르면 IllegalArgumentException을 던진다`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            createInitialProfileUseCase.createInitialProfile(firebaseUser, "wrongUserId")
        }
        assertThat(exception).hasMessageThat().contains("Firebase UID does not match with user ID")
    }
}