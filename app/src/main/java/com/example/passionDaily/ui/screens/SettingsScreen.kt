package com.example.passionDaily.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passionDaily.R
import com.example.passionDaily.ui.theme.Passion_DailyTheme
import com.example.passionDaily.util.CommonNavigationBar

@Composable
fun SettingsScreen(
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentScreen: NavigationBarScreens
) {
    SettingsScreenContent(
        onFavoritesClicked = onNavigateToFavorites,
        onQuoteClicked = onNavigateToQuote,
        onSettingsClicked = onNavigateToSettings,
        currentScreen = currentScreen
    )
}

@Composable
fun SettingsScreenContent(
    onFavoritesClicked: () -> Unit,
    onQuoteClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    currentScreen: NavigationBarScreens
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 45.dp),
        ) {
            Column(
                modifier = Modifier
                    .offset(x = 24.dp)
                    .align(Alignment.TopStart)
            ) {
                BackButton()
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                SettingsHeaderText()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 89.dp)
        ) {
            // 알림 설정
            SettingsCategoryHeader(text = "알림 설정")
            NotificationSettingItem()
            AdsNotificationSettingItem()

            // 프로필 설정
            SettingsCategoryHeader(text = "프로필 설정")
            NicknameSettingItem()
            LogoutSettingItem()

            // 고객 지원
            SettingsCategoryHeader(text = "고객 지원")
            SuggestionSettingItem()
            WithdrawalSettingItem()
            VersionInfoItem()

            // 약관 및 개인정보
            SettingsCategoryHeader(text = "약관 및 개인정보 처리 동의")
            TermsSettingItem()
            PrivacySettingItem()
            PrivacyConsentSettingItem()
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            CommonNavigationBar(
                currentScreen = currentScreen,
                onNavigateToFavorites = onFavoritesClicked,
                onNavigateToQuote = onQuoteClicked,
                onNavigateToSettings = onSettingsClicked
            )
        }
    }
}

@Composable
fun SettingsCategoryHeader(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(color = Color(0xFF0E1C41))
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFFFF),
            )
        )
    }
}

@Composable
fun SettingsHeaderText() {
    Text(
        text = "설정",
        style = TextStyle(
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
            fontWeight = FontWeight(400),
            color = Color(0xFFFFFFFF),
        )
    )
}

// 알림 설정 항목들
@Composable
fun NotificationSettingItem() {
    var isEnabled by remember { mutableStateOf(false) }
    CommonToggleItem(
        title = "알림 설정",
        isEnabled = isEnabled,
        onToggleChange = {
            isEnabled = it
            // 알림 설정 변경 로직
        }
    )
}

@Composable
fun AdsNotificationSettingItem() {
    var isEnabled by remember { mutableStateOf(false) }
    CommonToggleItem(
        title = "광고성 정보 수신 알림",
        isEnabled = isEnabled,
        onToggleChange = {
            isEnabled = it
            // 광고성 알림 설정 변경 로직
        }
    )
}

// 프로필 설정 항목들
@Composable
fun NicknameSettingItem() {
    CommonNavigationItem(
        title = "닉네임 변경하기",
        onClick = {
            // 닉네임 변경 화면으로 이동
        }
    )
}

@Composable
fun LogoutSettingItem() {
    CommonNavigationItem(
        title = "로그아웃",
        onClick = {
            // 로그아웃 처리
        }
    )
}

// 고객 지원 항목들
@Composable
fun SuggestionSettingItem() {
    CommonIconItem(
        title = "제안 보내기",
        icon = Icons.Filled.Email,
        onClick = {
            // 제안 작성 화면으로 이동
        }
    )
}

@Composable
fun WithdrawalSettingItem() {
    CommonNavigationItem(
        title = "회원 탈퇴",
        onClick = {
            // 회원 탈퇴 화면으로 이동
        }
    )
}

@Composable
fun VersionInfoItem() {
    CommonTextItem(
        title = "버전 정보",
        value = "1.1.1"
    )
}

// 약관 및 개인정보 항목들
@Composable
fun TermsSettingItem() {
    CommonNavigationItem(
        title = "이용약관",
        onClick = {
            // 이용약관 화면으로 이동
        }
    )
}

@Composable
fun PrivacySettingItem() {
    CommonNavigationItem(
        title = "개인정보 처리방침",
        onClick = {
            // 개인정보 처리방침 화면으로 이동
        }
    )
}

@Composable
fun PrivacyConsentSettingItem() {
    CommonNavigationItem(
        title = "개인정보 방침 동의 및 철회",
        onClick = {
            // 개인정보 동의 화면으로 이동
        }
    )
}

// 공통 컴포넌트들
@Composable
private fun CommonToggleItem(
    title: String,
    isEnabled: Boolean,
    onToggleChange: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFFFF),
            )
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggleChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0E1C41),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}

@Composable
private fun CommonNavigationItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .padding(horizontal = 24.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFFFF),
            )
        )
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
private fun CommonIconItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .padding(horizontal = 24.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFFFF),
            )
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun CommonTextItem(
    title: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFFFF),
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFFFF),
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    Passion_DailyTheme {
        SettingsScreen(
            onNavigateToFavorites = { },
            onNavigateToQuote = { },
            onNavigateToSettings = {  },
            currentScreen = NavigationBarScreens.SETTINGS
        )
    }
}



