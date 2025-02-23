package com.example.passionDaily.termsConsent.presentation.screen

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.R
import com.example.passionDaily.login.presentation.viewmodel.LoginViewModel
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.GrayScaleWhite
import com.example.passionDaily.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@Composable
fun TermsConsentScreen(
    loginViewModel: LoginViewModel,
    onNavigateToQuoteScreen: () -> Unit,
) {
    val isAgreeAllChecked by loginViewModel.isAgreeAllChecked.collectAsState()
    val consent by loginViewModel.consent.collectAsState()
    val authState by loginViewModel.authState.collectAsState()
    val formState by loginViewModel.signupFormState.collectAsState()
    val showDialog by loginViewModel.showEmailSentDialog.collectAsState()
    val isLoading by loginViewModel.isLoading.collectAsState()  // 추가!

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                color = PrimaryColor,
                modifier = Modifier.size(48.dp)
            )
        }
    }

    if (showDialog) {
        EmailSentDialog(
            email = formState.email,
            onDismiss = { loginViewModel.dismissEmailSentDialog() }
        )
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
                onCheckedChange = { loginViewModel.toggleAgreeAll() },
                isAgreeAll = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            CheckboxItem(
                text = stringResource(id = R.string.terms_of_service),
                isChecked = consent.termsOfService,
                onCheckedChange = { loginViewModel.toggleIndividualItem("termsOfService") },
                url = stringResource(id = R.string.terms_of_service_url),
                loginViewModel = loginViewModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            CheckboxItem(
                text = stringResource(id = R.string.privacy_policy_agreement),
                isChecked = consent.privacyPolicy,
                onCheckedChange = { loginViewModel.toggleIndividualItem("privacyPolicy") },
                url = stringResource(id = R.string.privacy_policy_url),
                loginViewModel = loginViewModel
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NextButton(
                enabled = consent.termsOfService &&
                        consent.privacyPolicy &&
                        formState.email.isNotBlank(),
                onNextClicked = {
                    loginViewModel.onNextButtonClick()
                }
            )
        }
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = PrimaryColor,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        if (showDialog) {
            EmailSentDialog(
                email = formState.email,
                onDismiss = { loginViewModel.dismissEmailSentDialog() }
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
    loginViewModel: LoginViewModel? = null
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onCheckedChange() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.check_with_circle),
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
                text = stringResource(id = R.string.look),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable {
                        loginViewModel?.openUrl(context, url)
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

@Composable
private fun EmailSentDialog(
    email: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFF1A2847),  // 배경색 변경
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "이메일을 확인하세요!",
                    color = GrayScaleWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = buildAnnotatedString {
                        append("이메일 주소를 확인하려면\n")
                        // SpanStyle 제거하고 모두 흰색으로 통일
                        append(email)
                        append("\n로 보낸 이메일의 링크를 클릭하세요.")
                    },
                    color = GrayScaleWhite,  // 전체 텍스트 흰색으로
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor
                    )
                ) {
                    Text("확인", color = Color.White)
                }
            }
        }
    }
}