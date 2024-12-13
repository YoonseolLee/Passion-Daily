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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.passionDaily.R
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.GrayScaleWhite
import com.example.passionDaily.ui.theme.Passion_DailyTheme
import com.example.passionDaily.ui.viewmodels.SharedSignInViewModel

@Composable
fun TermsConsentScreen(
    userProfileJson: String?,
    sharedSignInViewModel: SharedSignInViewModel = hiltViewModel(),
    onNavigateToAgeGenderSelection: () -> Unit = {},
) {
    LaunchedEffect(userProfileJson) {
        sharedSignInViewModel.verifyUserProfileJson(userProfileJson)
    }
    TermsConsentContent(
        onNextClicked = onNavigateToAgeGenderSelection
    )
}

@Composable
fun TermsConsentContent(
    onNextClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        Column(
            modifier =
            Modifier
                .offset(x = 34.dp, y = 100.dp)
                .align(Alignment.TopStart)
        ) {
            TermsConsentScreenHeaderTitle()
            Spacer(modifier = Modifier.height(38.dp))

            AgreeAll()
            Spacer(modifier = Modifier.height(20.dp))

            TermsOfService()
            Spacer(modifier = Modifier.height(16.dp))

            PrivacyPolicy()
            Spacer(modifier = Modifier.height(16.dp))

            MarketingConsent()
        }

        Column(
            modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            NextButton(
                onNextClicked = onNextClicked
            )
        }
    }
}

@Composable
fun TermsConsentScreenHeaderTitle() {
    Text(
        text = stringResource(id = R.string.terms_consent),
        style =
        TextStyle(
            fontSize = 32.sp,
            fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
            fontWeight = FontWeight(500),
            color = GrayScaleWhite,
        )
    )
}

@Composable
fun AgreeAll() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.check_with_circle),
            contentDescription = "check_with_circle",
            contentScale = ContentScale.Crop
        )
        Text(
            text = stringResource(id = R.string.agree_all),
            style =
            TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                fontWeight = FontWeight(500),
                color = GrayScaleWhite,
            )
        )
    }
}

@Composable
fun TermsOfService() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.check_without_circle),
            contentDescription = "check_without_circle",
            contentScale = ContentScale.Crop
        )
        Text(
            text = stringResource(id = R.string.terms_of_service),
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                fontWeight = FontWeight(500),
                color = GrayScaleWhite,
            )
        )
        View()
    }
}


@Composable
fun PrivacyPolicy() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.check_without_circle),
            contentDescription = "check_without_circle",
            contentScale = ContentScale.Crop
        )
        Text(
            text = stringResource(id = R.string.privacy_policy_agreement),
            style =
            TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                fontWeight = FontWeight(500),
                color = GrayScaleWhite,
            )
        )
        View()
    }
}

@Composable
fun MarketingConsent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.check_without_circle),
            contentDescription = "check_without_circle",
            contentScale = ContentScale.Crop
        )
        Text(
            text = stringResource(id = R.string.marketing_consent),
            style =
            TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                fontWeight = FontWeight(500),
                color = GrayScaleWhite,
            )
        )
        View()
    }
}

@Composable
fun View() {
    Text(
        text = "보기",
        style = TextStyle(
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
            color = Color(0xFFB3B3B3),
            textDecoration = TextDecoration.Underline,
        )
    )
}

@Composable
fun NextButton(
    onNextClicked: () -> Unit
) {
    Row(
        modifier =
        Modifier
            .width(345.dp)
            .height(54.dp)
            .background(color = GrayScaleWhite, shape = RoundedCornerShape(size = 10.dp))
            .clickable(onClick = onNextClicked),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.next),
            textAlign = TextAlign.Center,
            style =
            TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                color = Color(0xFF000000),
            ),
        )
    }
}

//@Preview
//@Composable
//fun PreviewTermsConsentContent() {
//    Passion_DailyTheme {
//        TermsConsentContent()
//    }
//}



