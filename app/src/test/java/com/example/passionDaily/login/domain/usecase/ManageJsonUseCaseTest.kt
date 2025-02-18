package com.example.passionDaily.login.domain.usecase

import com.example.passionDaily.login.domain.model.UserConsent
import com.example.passionDaily.login.domain.model.UserProfileKey
import com.example.passionDaily.login.stateholder.UserProfileStateHolder
import com.example.passionDaily.util.MainCoroutineRule
import com.example.passionDaily.util.mapper.UserProfileMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ManageJsonUseCaseTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var useCase: ManageJsonUseCase
    private val mapper = mockk<UserProfileMapper>()
    private val stateHolder = mockk<UserProfileStateHolder>()

    private val validJson = """
        {
            "privacyPolicyEnabled": false,
            "termsOfServiceEnabled": false
        }
    """.trimIndent()

    private val userConsent = UserConsent(privacyPolicy = true, termsOfService = true)

    @Before
    fun setup() {
        useCase = ManageJsonUseCase(mapper, stateHolder)
    }

    @Test
    fun `유효한 JSON 검증시 true 반환`() = mainCoroutineRule.runTest {
        // given
        coEvery { stateHolder.updateIsJsonValid(any()) } just Runs

        // when
        val result = useCase.verifyJson(validJson)

        // then
        assertThat(result).isTrue()
        coVerify { stateHolder.updateIsJsonValid(true) }
    }

    @Test
    fun `유효하지 않은 JSON 검증시 false 반환`() = mainCoroutineRule.runTest {
        // given
        coEvery { stateHolder.updateIsJsonValid(any()) } just Runs

        // when
        val result = useCase.verifyJson("{")

        // then
        assertThat(result).isFalse()
        coVerify { stateHolder.updateIsJsonValid(false) }
    }

    @Test
    fun `null JSON 검증시 false 반환`() = mainCoroutineRule.runTest {
        // given
        coEvery { stateHolder.updateIsJsonValid(any()) } just Runs

        // when
        val result = useCase.verifyJson(null)

        // then
        assertThat(result).isFalse()
        coVerify { stateHolder.updateIsJsonValid(false) }
    }

    @Test
    fun `동의 업데이트 시 새로운 JSON 반환`() = mainCoroutineRule.runTest {
        // given
        val _isJsonValid = MutableStateFlow(true)
        every { stateHolder.isJsonValid } returns _isJsonValid

        // when
        val testJsonString = """
    {
        "${UserProfileKey.ID.key}": "test-id",
        "${UserProfileKey.NAME.key}": "hello",
        "${UserProfileKey.PRIVACY_POLICY_ENABLED.key}": null,
        "${UserProfileKey.TERMS_OF_SERVICE_ENABLED.key}": null
    }
    """.trimIndent()

        val resultString = useCase.updateUserProfileWithConsent(testJsonString, userConsent)

        // then
        assertThat(resultString).isNotNull()
        val resultJson = JSONObject(resultString)
        println("Result parsed as JSON: $resultJson")

        assertThat(resultJson.getBoolean(UserProfileKey.PRIVACY_POLICY_ENABLED.key)).isTrue()
        assertThat(resultJson.getBoolean(UserProfileKey.TERMS_OF_SERVICE_ENABLED.key)).isTrue()
    }

    @Test
    fun `유효한 사용자 프로필에서 정보 추출 성공`() = mainCoroutineRule.runTest {
        // given
        val validUserProfileJson = """
        {
            "${UserProfileKey.ID.key}": "test-user-id",
            "${UserProfileKey.NAME.key}": "hello",
            "${UserProfileKey.PRIVACY_POLICY_ENABLED.key}": true,
            "${UserProfileKey.TERMS_OF_SERVICE_ENABLED.key}": false
        }
    """.trimIndent()

        val expectedMap = mapOf(
            UserProfileKey.ID.key to "test-user-id",
            UserProfileKey.NAME.key to "hello",
            UserProfileKey.PRIVACY_POLICY_ENABLED.key to true,
            UserProfileKey.TERMS_OF_SERVICE_ENABLED.key to false
        )

        coEvery { mapper.mapFromJson(any()) } returns expectedMap

        // when
        val (profileMap, userId) = useCase.extractUserInfo(validUserProfileJson)

        // then
        assertThat(profileMap).isEqualTo(expectedMap)
        assertThat(userId).isEqualTo("test-user-id")
        coVerify { mapper.mapFromJson(validUserProfileJson) }
    }

    @Test
    fun `동의 업데이트시 잘못된 JSON이면 예외 발생`() = mainCoroutineRule.runTest {
        // given
        val invalidJson = "{ invalid }"
        val _isJsonValid = MutableStateFlow(false)
        every { stateHolder.isJsonValid } returns _isJsonValid

        // when
        var actualException: Exception? = null
        try {
            runBlocking {
                useCase.updateUserProfileWithConsent(invalidJson, userConsent)
            }
        } catch (e: Exception) {
            actualException = e
        }

        // then
        assertThat(actualException).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `사용자 정보 추출 성공`() = mainCoroutineRule.runTest {
        // given
        val json = """
        {
            "${UserProfileKey.ID.key}": "test-id",
            "${UserProfileKey.NAME.key}": "hello"
        }
    """.trimIndent()

        val profileMap = mapOf(
            UserProfileKey.ID.key to "test-id",
            UserProfileKey.NAME.key to "hello"
        )

        coEvery { mapper.mapFromJson(any()) } returns profileMap

        // when
        val result = runBlocking {
            useCase.extractUserInfo(json)
        }

        // then
        assertThat(result.first).isEqualTo(profileMap)
        assertThat(result.second).isEqualTo("test-id")
    }

    @Test
    fun `사용자 ID 없을 경우 정보 추출 실패`() = mainCoroutineRule.runTest {
        // given
        val json = """
        {
            "${UserProfileKey.NAME.key}": "hello"
        }
    """.trimIndent()

        val profileMap = mapOf(
            UserProfileKey.NAME.key to "hello"
        )

        coEvery { mapper.mapFromJson(any()) } returns profileMap

        // when
        var actualException: Exception? = null
        try {
            runBlocking {
                useCase.extractUserInfo(json)
            }
        } catch (e: Exception) {
            actualException = e
        }

        // then
        assertThat(actualException).isInstanceOf(IllegalArgumentException::class.java)
    }
}
