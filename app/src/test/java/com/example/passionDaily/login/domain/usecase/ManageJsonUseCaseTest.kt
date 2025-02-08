package com.example.passionDaily.login.domain.usecase

import android.util.Log
import com.example.passionDaily.login.domain.model.UserConsent
import com.example.passionDaily.login.stateholder.UserProfileStateHolder
import com.example.passionDaily.util.MainCoroutineRule
import com.example.passionDaily.util.mapper.UserProfileMapper
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ManageJsonUseCaseTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var manageJsonUseCase: ManageJsonUseCase

    @MockK
    private lateinit var userProfileMapper: UserProfileMapper

    @MockK
    private lateinit var userProfileStateHolder: UserProfileStateHolder

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        // Log 모킹
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        // StateFlow 모킹
        every { userProfileStateHolder.isJsonValid } returns MutableStateFlow(true)
        every { userProfileStateHolder.updateIsJsonValid(any()) } just Runs

        manageJsonUseCase = ManageJsonUseCase(userProfileMapper, userProfileStateHolder)
    }

    @Test
    fun `verifyJson - 유효한 JSON이면 true 반환`() = coroutineRule.runTest {
        // given
        val validJson = """{"id":"123","email":"test@example.com"}"""
        every { userProfileStateHolder.updateIsJsonValid(true) } just Runs

        // when
        val result = manageJsonUseCase.verifyJson(validJson)

        // then
        assertTrue(result)
        verify { userProfileStateHolder.updateIsJsonValid(true) }
    }

    @Test
    fun `verifyJson - 잘못된 JSON이면 false 반환`() = coroutineRule.runTest {
        // given
        val invalidJson = """{"id":"123", "email": "test@example.com"""  // 닫는 중괄호 없음
        every { userProfileStateHolder.updateIsJsonValid(false) } just Runs

        // when
        val result = manageJsonUseCase.verifyJson(invalidJson)

        // then
        assertFalse(result)
        verify { userProfileStateHolder.updateIsJsonValid(false) }
    }

    @Test
    fun `updateUserProfileWithConsent - 동의 정보 업데이트 성공`() = coroutineRule.runTest {
        // given
        val userProfileJson = """{"id":"123","email":"test@example.com"}"""
        val consent = UserConsent(privacyPolicy = true, termsOfService = true)
        every { userProfileStateHolder.isJsonValid.value } returns true

        // when
        val updatedJson = manageJsonUseCase.updateUserProfileWithConsent(userProfileJson, consent)

        // then
        assertNotNull(updatedJson)
        assertTrue(updatedJson!!.contains("\"privacy_policy_enabled\":true"))
        assertTrue(updatedJson.contains("\"terms_of_service_enabled\":true"))
    }
}

