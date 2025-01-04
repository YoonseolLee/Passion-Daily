package com.example.passionDaily.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passionDaily.R
import com.example.passionDaily.ui.theme.Passion_DailyTheme
import com.example.passionDaily.ui.viewmodels.SettingsViewModel
import com.example.passionDaily.util.CommonNavigationBar
import java.time.LocalTime

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentScreen: NavigationBarScreens
) {
    SettingsScreenContent(
        viewModel = settingsViewModel,
        onFavoritesClicked = onNavigateToFavorites,
        onQuoteClicked = onNavigateToQuote,
        onSettingsClicked = onNavigateToSettings,
        currentScreen = currentScreen
    )
}

@Composable
fun SettingsScreenContent(
    viewModel: SettingsViewModel,
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
            NotificationSettingItem(viewModel)
            NotificationTimeSettingItem(viewModel)

            // 프로필 설정
            SettingsCategoryHeader(text = "계정 관리")
            LoginSettingItem(
                viewModel,
                onNavigateToLogin = onSettingsClicked
            )

            LogoutSettingItem(
                viewModel,
                onNavigateToQuote = onQuoteClicked
            )

            // 고객 지원
            SettingsCategoryHeader(text = "고객 지원")
            SuggestionSettingItem(viewModel)
            WithdrawalSettingItem(viewModel, onNavigateToQuote = onQuoteClicked)
            VersionInfoItem()

            // 약관 및 개인정보
            SettingsCategoryHeader(text = "약관 및 개인정보 처리 동의")
            TermsSettingItem()
            PrivacySettingItem()
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
fun NotificationSettingItem(
    viewModel: SettingsViewModel
) {
    val isEnabled by viewModel.notificationEnabled.collectAsState()

    CommonToggleItem(
        title = "데일리 명언 알림 설정",
        isEnabled = isEnabled,
        onToggleChange = { enabled ->
            viewModel.updateNotificationSettings(enabled)
        }
    )
}

@Composable
fun NotificationTimeSettingItem(
    viewModel: SettingsViewModel
) {
    val notificationTime by viewModel.notificationTime.collectAsState()
    val context = LocalContext.current

    var showTimePickerDialog by remember { mutableStateOf(false) }

    if (showTimePickerDialog) {
        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val selectedTime = LocalTime.of(selectedHour, selectedMinute)
                viewModel.updateNotificationTime(selectedTime)
                showTimePickerDialog = false
            },
            notificationTime?.hour ?: 8,
            notificationTime?.minute ?: 0,
            true
        ).show()
    }

    CommonTextItem(
        title = "알림 시간 설정",
        value = notificationTime?.toString() ?: "08:00",
        onClick = { showTimePickerDialog = true }
    )
}

@Composable
fun LoginSettingItem(
    viewModel: SettingsViewModel,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val toastMessage by viewModel.toastMessage.collectAsState()
    val shouldNavigateToLogin by viewModel.navigateToLogin.collectAsState()

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    LaunchedEffect(shouldNavigateToLogin) {
        if (shouldNavigateToLogin) {
            onNavigateToLogin()
            viewModel.onNavigatedToLogin()
        }
    }

    CommonNavigationItem(
        title = "로그인",
        onClick = { viewModel.logIn() }
    )
}

@Composable
fun LogoutSettingItem(
    viewModel: SettingsViewModel,
    onNavigateToQuote: () -> Unit
) {
    val context = LocalContext.current
    val toastMessage by viewModel.toastMessage.collectAsState()
    val shouldNavigateToQuote by viewModel.navigateToQuote.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    LaunchedEffect(shouldNavigateToQuote) {
        if (shouldNavigateToQuote) {
            onNavigateToQuote()
            viewModel.onNavigatedToQuote()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "로그아웃",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF)
                    )
                )
            },
            text = {
                Text(
                    text = "로그아웃 하시겠습니까?",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color(0xFFFFFFFF)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logOut()
                        showLogoutDialog = false
                    }
                ) {
                    Text(
                        "네",
                        color = Color(0xFFFFFFFF)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        "아니오",
                        color = Color(0xFFFFFFFF)
                    )
                }
            },
            containerColor = Color(0xFF0E1C41),
            shape = RoundedCornerShape(8.dp)
        )
    }

    CommonNavigationItem(
        title = "로그아웃",
        onClick = { showLogoutDialog = true }
    )
}


// 고객 지원 항목들
@Composable
fun SuggestionSettingItem(
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val emailError by viewModel.emailError.collectAsState()

    LaunchedEffect(emailError) {
        emailError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    CommonIconItem(
        title = "제안 보내기",
        icon = Icons.Filled.Email,
        onClick = {
            try {
                val intent = viewModel.createEmailIntent()
                context.startActivity(Intent.createChooser(intent, "이메일 앱 선택"))
            } catch (e: Exception) {
                viewModel.setError("이메일 전송에 실패했습니다.")
            }
        }
    )
}

@Composable
fun WithdrawalSettingItem(
    viewModel: SettingsViewModel,
    onNavigateToQuote: () -> Unit
) {
    val context = LocalContext.current
    val toastMessage by viewModel.toastMessage.collectAsState()
    val shouldNavigateToQuote by viewModel.navigateToQuote.collectAsState()
    val showWithdrawalDialog by viewModel.showWithdrawalDialog.collectAsState()

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    LaunchedEffect(shouldNavigateToQuote) {
        if (shouldNavigateToQuote) {
            onNavigateToQuote()
            viewModel.onNavigatedToQuote()
        }
    }

    if (showWithdrawalDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.updateShowWithdrawalDialog(false) },
            title = {
                Text(
                    text = "회원 탈퇴",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF)
                    )
                )
            },
            text = {
                Text(
                    text = "정말로 탈퇴하시겠습니까?\n탈퇴 시 모든 데이터가 삭제됩니다.",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color(0xFFFFFFFF)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.withdrawUser()
                        viewModel.updateShowWithdrawalDialog(false)
                    }
                ) {
                    Text(
                        "네",
                        color = Color(0xFFFFFFFF)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.updateShowWithdrawalDialog(false) }
                ) {
                    Text(
                        "아니오",
                        color = Color(0xFFFFFFFF)
                    )
                }
            },
            containerColor = Color(0xFF0E1C41),
            shape = RoundedCornerShape(8.dp)
        )
    }

    CommonNavigationItem(
        title = "회원 탈퇴",
        onClick = { viewModel.updateShowWithdrawalDialog(true) }
    )
}

@Composable
fun VersionInfoItem() {
    val context = LocalContext.current
    val versionName = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    CommonTextItem(
        title = "버전 정보",
        value = versionName,
        onClick = {}
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
        title = "마케팅 정보 수신 동의",
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
        Image(
            painter = painterResource(R.drawable.chevron_right),
            contentDescription = "chevron_right",
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
    value: String,
    onClick: () -> Unit
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
            modifier = Modifier.clickable(onClick = onClick),
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
            onNavigateToSettings = { },
            currentScreen = NavigationBarScreens.SETTINGS
        )
    }
}
