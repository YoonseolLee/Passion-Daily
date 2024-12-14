package com.example.passionDaily.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.passionDaily.R
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.GrayScaleWhite
import com.example.passionDaily.ui.theme.PrimaryColor
import com.example.passionDaily.ui.viewmodels.SharedSignInViewModel
import org.json.JSONObject

@Composable
fun TermsConsentScreen(
    userProfileJson: String? = null,
    sharedSignInViewModel: SharedSignInViewModel = hiltViewModel(),
    onNavigateToAgeGenderSelection: () -> Unit = {}
) {
    // 사용자 프로필 JSON 검증
    LaunchedEffect(userProfileJson) {
        sharedSignInViewModel.verifyUserProfileJson(userProfileJson)
    }

    // 약관 동의 화면 상태 관리
    var isAgreeAllChecked by remember { mutableStateOf(false) }
    var termsOfServiceChecked by remember { mutableStateOf(false) }
    var privacyPolicyChecked by remember { mutableStateOf(false) }
    var marketingConsentChecked by remember { mutableStateOf(false) }

    // 전체 동의 토글 핸들러
    val toggleAgreeAll: () -> Unit = {
        isAgreeAllChecked = !isAgreeAllChecked
        termsOfServiceChecked = isAgreeAllChecked
        privacyPolicyChecked = isAgreeAllChecked
        marketingConsentChecked = isAgreeAllChecked
    }

    // 개별 항목 토글 핸들러
    val toggleIndividualItem: (String) -> Unit = { item ->
        when (item) {
            "termsOfService" -> {
                termsOfServiceChecked = !termsOfServiceChecked
                isAgreeAllChecked = termsOfServiceChecked &&
                        privacyPolicyChecked &&
                        marketingConsentChecked
            }
            "privacyPolicy" -> {
                privacyPolicyChecked = !privacyPolicyChecked
                isAgreeAllChecked = termsOfServiceChecked &&
                        privacyPolicyChecked &&
                        marketingConsentChecked
            }
            "marketingConsent" -> {
                marketingConsentChecked = !marketingConsentChecked
                isAgreeAllChecked = termsOfServiceChecked &&
                        privacyPolicyChecked &&
                        marketingConsentChecked
            }
        }
    }

//    // JSON 생성 함수
//    val createConsentJson: () -> String = {
//        JSONObject().apply {
//            put("agreeAll", isAgreeAllChecked)
//            put("termsOfService", termsOfServiceChecked)
//            put("privacyPolicy", privacyPolicyChecked)
//            put("marketingConsent", marketingConsentChecked)
//        }.toString()
//    }
//
//    // 다음 버튼 클릭 핸들러
//    val handleNextClick: () -> Unit = {
//        val consentJson = createConsentJson()
//        sharedSignInViewModel.saveConsentJson(consentJson)
//        onNavigateToAgeGenderSelection()
//    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        Column(
            modifier = Modifier
                .offset(x = 34.dp, y = 100.dp)
                .align(Alignment.TopStart)
        ) {
            // 화면 헤더
            Text(
                text = stringResource(id = R.string.terms_consent),
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight(500),
                    color = GrayScaleWhite
                )
            )

            Spacer(modifier = Modifier.height(38.dp))

            // 전체 동의 체크박스
            CheckboxItem(
                text = stringResource(id = R.string.agree_all),
                isChecked = isAgreeAllChecked,
                onCheckedChange = { toggleAgreeAll() },
                isAgreeAll = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 서비스 이용약관 체크박스
            CheckboxItem(
                text = stringResource(id = R.string.terms_of_service),
                isChecked = termsOfServiceChecked,
                onCheckedChange = { toggleIndividualItem("termsOfService") },
                url = stringResource(id = R.string.terms_of_service_url),
                sharedSignInViewModel = sharedSignInViewModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 개인정보 처리방침 체크박스
            CheckboxItem(
                text = stringResource(id = R.string.privacy_policy_agreement),
                isChecked = privacyPolicyChecked,
                onCheckedChange = { toggleIndividualItem("privacyPolicy") },
                url = stringResource(id = R.string.privacy_policy_url),
                sharedSignInViewModel = sharedSignInViewModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 마케팅 수신 동의 체크박스
            CheckboxItem(
                text = stringResource(id = R.string.marketing_consent),
                isChecked = marketingConsentChecked,
                onCheckedChange = { toggleIndividualItem("marketingConsent") },
                url = stringResource(id = R.string.marketing_consent_url),
                sharedSignInViewModel = sharedSignInViewModel
            )
        }

        // 다음 버튼
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NextButton(
                enabled = termsOfServiceChecked && privacyPolicyChecked,
                onNextClicked = handleNextClick
            )
        }
    }
}

@Composable
fun CheckboxItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: () -> Unit,
    isAgreeAll: Boolean = false,
    url: String? = null,
    sharedSignInViewModel: SharedSignInViewModel? = null
) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.check_without_circle),
            contentDescription = "checkbox",
            modifier = Modifier
                .size(24.dp)
                .clickable { onCheckedChange() },
            colorFilter = if (isChecked)
                ColorFilter.tint(
                    if (isAgreeAll) PrimaryColor
                    else PrimaryColor
                )
            else
                null
        )

        Text(
            text = text,
            style = TextStyle(
                fontSize = if (isAgreeAll) 18.sp else 16.sp,
                fontWeight = FontWeight(500),
                color = GrayScaleWhite
            ),
            modifier = Modifier.weight(1f)
        )

        url?.let {
            Text(
                text = "보기",
                modifier = Modifier.clickable {
                    sharedSignInViewModel?.openUrl(context, it)
                },
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFFB3B3B3),
                    textDecoration = TextDecoration.Underline
                )
            )
        }
    }
}

@Composable
fun NextButton(
    enabled: Boolean,
    onNextClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(345.dp)
            .height(54.dp)
            .background(
                color = if (enabled) GrayScaleWhite else Color.Gray,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = enabled) { onNextClicked() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.next),
            style = TextStyle(
                fontSize = 18.sp,
                color = Color.Black
            )
        )
    }
}

