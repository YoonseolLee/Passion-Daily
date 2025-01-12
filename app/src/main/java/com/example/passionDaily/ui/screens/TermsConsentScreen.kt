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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.passionDaily.R
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.GrayScaleWhite
import com.example.passionDaily.ui.theme.PrimaryColor
import com.example.passionDaily.ui.viewmodels.SharedSignInViewModel

@Composable
fun TermsConsentScreen(
    userProfileJson: String? = null,
    sharedSignInViewModel: SharedSignInViewModel = hiltViewModel(),
    onNavigateToQuoteScreen: () -> Unit
) {
    val isAgreeAllChecked by sharedSignInViewModel.isAgreeAllChecked.collectAsState()
    val termsOfServiceChecked by sharedSignInViewModel.termsOfServiceChecked.collectAsState()
    val privacyPolicyChecked by sharedSignInViewModel.privacyPolicyChecked.collectAsState()
    val userProfileJsonV2 by sharedSignInViewModel.userProfileJsonV2.collectAsState()

    // 초기 userProfileJson 검증
    LaunchedEffect(userProfileJson) {
        sharedSignInViewModel.verifyUserProfileJson(userProfileJson)
    }

    // userProfileJsonV2가 업데이트되면 화면 전환
    LaunchedEffect(userProfileJsonV2) {
        userProfileJsonV2?.let {
            onNavigateToQuoteScreen()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 34.dp)
                .padding(top = 100.dp)
                .align(Alignment.TopStart)
        ) {
            Text(
                text = stringResource(id = R.string.terms_consent),
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight(500),
                    color = GrayScaleWhite
                )
            )

            Spacer(modifier = Modifier.height(38.dp))

            CheckboxItem(
                text = stringResource(id = R.string.agree_all),
                isChecked = isAgreeAllChecked,
                onCheckedChange = { sharedSignInViewModel.toggleAgreeAll() },
                isAgreeAll = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            CheckboxItem(
                text = stringResource(id = R.string.terms_of_service),
                isChecked = termsOfServiceChecked,
                onCheckedChange = { sharedSignInViewModel.toggleIndividualItem("termsOfService") },
                url = stringResource(id = R.string.terms_of_service_url),
                sharedSignInViewModel = sharedSignInViewModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            CheckboxItem(
                text = stringResource(id = R.string.privacy_policy_agreement),
                isChecked = privacyPolicyChecked,
                onCheckedChange = { sharedSignInViewModel.toggleIndividualItem("privacyPolicy") },
                url = stringResource(id = R.string.privacy_policy_url),
                sharedSignInViewModel = sharedSignInViewModel
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NextButton(
                enabled = termsOfServiceChecked && privacyPolicyChecked,
                onNextClicked = {
                    sharedSignInViewModel.handleNextClick(userProfileJson)
                    userProfileJsonV2?.let {
                        onNavigateToQuoteScreen()
                    }
                }
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 체크박스와 텍스트를 포함하는 영역
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onCheckedChange() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.check_without_circle),
                contentDescription = "checkbox",
                modifier = Modifier.size(24.dp),
                colorFilter = if (isChecked)
                    ColorFilter.tint(PrimaryColor)
                else
                    null
            )

            Text(
                text = text,
                style = TextStyle(
                    fontSize = if (isAgreeAll) 18.sp else 16.sp,
                    fontWeight = FontWeight(500),
                    color = GrayScaleWhite
                )
            )
        }

        // "보기" 텍스트
        if (url != null && !isAgreeAll) {
            Text(
                text = "보기",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable {
                        sharedSignInViewModel?.openUrl(context, url)
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