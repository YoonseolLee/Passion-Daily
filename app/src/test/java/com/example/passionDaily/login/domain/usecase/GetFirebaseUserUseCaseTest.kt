package com.example.passionDaily.login.domain.usecase

import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertThrows
import org.junit.Rule

class GetFirebaseUserUseCaseTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var getFirebaseUserUseCase: GetFirebaseUserUseCase
    private val stringProvider: StringProvider = mockk()
    private val authResult: AuthResult = mockk()
    private val firebaseUser: FirebaseUser = mockk()

    @Before
    fun setUp() {
        getFirebaseUserUseCase = GetFirebaseUserUseCase(stringProvider)
        every { authResult.user } returns firebaseUser
        every { firebaseUser.uid } returns "testUserId"
    }

    @Test
    fun `AuthResult에서 FirebaseUser가 정상 반환된다`() = mainCoroutineRule.runTest {
        val result = getFirebaseUserUseCase.getFirebaseUser(authResult)
        assertThat(result).isEqualTo(firebaseUser)
    }

    @Test
    fun `AuthResult의 user가 null이면 예외 발생`() = mainCoroutineRule.runTest {
        every { authResult.user } returns null
        every { stringProvider.getString(any()) } returns "Firebase user is null"

        val exception = assertThrows(IllegalArgumentException::class.java) {
            getFirebaseUserUseCase.getFirebaseUser(authResult)
        }
        assertThat(exception).hasMessageThat().contains("Firebase user is null")
    }
}