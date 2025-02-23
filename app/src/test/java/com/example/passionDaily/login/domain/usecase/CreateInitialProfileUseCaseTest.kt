package com.example.passionDaily.login.domain.usecase

import com.example.passionDaily.login.domain.model.UserProfileKey
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertThrows
import org.junit.Rule

class CreateInitialProfileUseCaseTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var createInitialProfileUseCase: CreateInitialProfileUseCase
    private val firebaseUser: FirebaseUser = mockk()

    @Before
    fun setUp() {
        createInitialProfileUseCase = CreateInitialProfileUseCase()
        every { firebaseUser.uid } returns "testUserId"
        every { firebaseUser.displayName } returns "hello"
    }

    @Test
    fun `유효한 FirebaseUser와 userId를 제공하면 프로필 맵이 올바르게 생성된다`() = mainCoroutineRule.runTest{
        val userId = "testUserId"
        val profileMap = createInitialProfileUseCase.createInitialProfile(firebaseUser, userId)

        assertThat(profileMap).isNotEmpty()
        assertThat(profileMap[UserProfileKey.ID.key]).isEqualTo(userId)
        assertThat(profileMap[UserProfileKey.NAME.key]).isEqualTo("hello")
        assertThat(profileMap[UserProfileKey.ROLE.key]).isEqualTo(UseCaseConstants.UserProfileConstants.ROLE_USER)
    }

    @Test
    fun `FirebaseUser의 displayName이 null이면 IllegalArgumentException을 던진다`() = mainCoroutineRule.runTest {
        every { firebaseUser.displayName } returns null
        val exception = assertThrows(IllegalArgumentException::class.java) {
            createInitialProfileUseCase.createInitialProfile(firebaseUser, "testUserId")
        }
        assertThat(exception).hasMessageThat().contains("Firebase user name cannot be blank")
    }

    @Test
    fun `FirebaseUser의 UID와 userId가 다르면 IllegalArgumentException을 던진다`() = mainCoroutineRule.runTest{
        val exception = assertThrows(IllegalArgumentException::class.java) {
            createInitialProfileUseCase.createInitialProfile(firebaseUser, "wrongUserId")
        }
        assertThat(exception).hasMessageThat().contains("Firebase UID does not match with user ID")
    }

    @Test
    fun `userId가 비어있으면 IllegalArgumentException을 던진다`() = mainCoroutineRule.runTest{
        val exception = assertThrows(IllegalArgumentException::class.java) {
            createInitialProfileUseCase.createInitialProfile(firebaseUser, "")
        }
        assertThat(exception).hasMessageThat().contains("User ID cannot be blank")
    }

    @Test
    fun `FirebaseUser의 UID가 비어있으면 IllegalArgumentException을 던진다`() = mainCoroutineRule.runTest{
        every { firebaseUser.uid } returns ""
        val exception = assertThrows(IllegalArgumentException::class.java) {
            createInitialProfileUseCase.createInitialProfile(firebaseUser, "testUserId")
        }
        assertThat(exception).hasMessageThat().contains("Firebase user ID cannot be blank")
    }
}