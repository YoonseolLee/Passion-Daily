package com.example.passionDaily.login.manager

import app.cash.turbine.test
import com.example.passionDaily.login.domain.model.UserConsent
import com.example.passionDaily.login.stateholder.ConsentStateHolder
import com.example.passionDaily.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Test
import org.junit.Rule

@ExperimentalCoroutinesApi
class UserConsentManagerTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var consentStateHolder: ConsentStateHolder
    private lateinit var userConsentManager: UserConsentManager

    @Before
    fun setup() {
        consentStateHolder = object : ConsentStateHolder {
            private val _consent = MutableStateFlow(UserConsent(
                termsOfService = false,
                privacyPolicy = false
            ))
            private val _isAgreeAllChecked = MutableStateFlow(false)

            override val consent: StateFlow<UserConsent> = _consent
            override val isAgreeAllChecked: StateFlow<Boolean> = _isAgreeAllChecked

            override fun updateConsent(consent: UserConsent) {
                _consent.value = consent
            }

            override fun updateAgreeAllChecked(checked: Boolean) {
                _isAgreeAllChecked.value = checked
            }

            override fun clearConsent() {
                _consent.value = UserConsent(
                    termsOfService = false,
                    privacyPolicy = false
                )
                _isAgreeAllChecked.value = false
            }
        }
        userConsentManager = UserConsentManagerImpl(consentStateHolder)
    }

    @Test
    fun `전체 동의 토글 시 모든 항목이 동일한 상태로 변경되어야 한다`() = mainCoroutineRule.runTest {
        // given
        with(userConsentManager.consent.value) {
            assertThat(termsOfService).isFalse()
            assertThat(privacyPolicy).isFalse()
        }
        assertThat(userConsentManager.isAgreeAllChecked.value).isFalse()

        // When: 전체 동의를 활성화
        userConsentManager.toggleAgreeAll()

        // Then: 모든 항목이 true로 변경되었는지 확인
        with(userConsentManager.consent.value) {
            assertThat(termsOfService).isTrue()
            assertThat(privacyPolicy).isTrue()
            // isAllAgreed 속성도 함께 검증
            assertThat(isAllAgreed).isTrue()
        }
        assertThat(userConsentManager.isAgreeAllChecked.value).isTrue()

        // When: 전체 동의를 비활성화
        userConsentManager.toggleAgreeAll()

        // Then: 모든 항목이 false로 변경되었는지 확인
        with(userConsentManager.consent.value) {
            assertThat(termsOfService).isFalse()
            assertThat(privacyPolicy).isFalse()
            assertThat(isAllAgreed).isFalse()
        }
        assertThat(userConsentManager.isAgreeAllChecked.value).isFalse()
    }

    @Test
    fun `개별 항목 토글 시 해당 항목만 변경되어야 한다`() = mainCoroutineRule.runTest {
        // When: 이용약관만 동의
        userConsentManager.toggleIndividualItem(UserConsent::termsOfService.name)

        // Then: 이용약관만 true이고 나머지는 false인지 확인
        with(userConsentManager.consent.value) {
            assertThat(termsOfService).isTrue()
            assertThat(privacyPolicy).isFalse()
            assertThat(isAllAgreed).isFalse()  // 일부만 동의했으므로 false
        }
        assertThat(userConsentManager.isAgreeAllChecked.value).isFalse()

        // When: 개인정보 처리방침도 동의
        userConsentManager.toggleIndividualItem(UserConsent::privacyPolicy.name)

        // Then: 모든 항목이 true가 되어 전체 동의 상태가 되어야 함
        with(userConsentManager.consent.value) {
            assertThat(termsOfService).isTrue()
            assertThat(privacyPolicy).isTrue()
            assertThat(isAllAgreed).isTrue()
        }
        assertThat(userConsentManager.isAgreeAllChecked.value).isTrue()
    }

    @Test
    fun `잘못된 항목 이름으로 토글 시 상태가 변경되지 않아야 한다`() = mainCoroutineRule.runTest {
        // Given: 초기 상태 저장
        val initialConsent = userConsentManager.consent.value
        val initialAgreeAll = userConsentManager.isAgreeAllChecked.value

        // When: 존재하지 않는 항목 이름으로 토글 시도
        userConsentManager.toggleIndividualItem("nonexistent")

        // Then: 상태가 변경되지 않아야 함
        assertThat(userConsentManager.consent.value).isEqualTo(initialConsent)
        assertThat(userConsentManager.isAgreeAllChecked.value).isEqualTo(initialAgreeAll)
    }

    @Test
    fun `Flow 상태 변경을 Turbine으로 테스트`() = mainCoroutineRule.runTest {
        // Turbine을 사용하여 Flow 상태 변경을 테스트
        userConsentManager.consent.test {
            // 초기 상태 확인
            var emission = awaitItem()
            assertThat(emission.termsOfService).isFalse()
            assertThat(emission.privacyPolicy).isFalse()

            // 전체 동의 토글
            userConsentManager.toggleAgreeAll()
            emission = awaitItem()
            assertThat(emission.termsOfService).isTrue()
            assertThat(emission.privacyPolicy).isTrue()

            // 더 이상의 변경이 없음을 확인
            cancelAndConsumeRemainingEvents()
        }
    }
}