package com.example.passionDaily.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.passionDaily.R
import com.example.passionDaily.settings.presentation.viewmodel.SettingsViewModel
import com.example.passionDaily.util.CommonNavigationBar
import java.time.LocalTime
import android.provider.Settings
import android.Manifest
import androidx.compose.material3.CircularProgressIndicator
import androidx.core.content.ContextCompat
import com.example.passionDaily.ui.theme.PrimaryColor

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    currentScreen: NavigationBarScreens,
    onBack: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScreenContent(
            viewModel = settingsViewModel,
            onFavoritesClicked = onNavigateToFavorites,
            onQuoteClicked = onNavigateToQuote,
            onSettingsClicked = onNavigateToSettings,
            onNavigateToLogin = onNavigateToLogin,
            currentScreen = currentScreen,
            onBack = onBack
        )

        if (settingsViewModel.isLoading.collectAsState().value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryColor,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsScreenContent(
    viewModel: SettingsViewModel,
    onFavoritesClicked: () -> Unit,
    onQuoteClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onNavigateToLogin: () -> Unit,
    currentScreen: NavigationBarScreens,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .padding(start = 24.dp)
            ) {
                BackButton(onBack = onBack)
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                SettingsHeaderText()
            }

            Box(
                modifier = Modifier.width(72.dp)
            )
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
            if (currentUser != null) {
                LogoutSettingItem(viewModel, onNavigateToQuote = onQuoteClicked)
            } else {
                LoginSettingItem(viewModel, onNavigateToLogin = onNavigateToLogin)
            }

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

        // 하단 네비게이션 바
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

@Composable
fun NotificationSettingItem(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val isEnabled by viewModel.notificationEnabled.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = "알림 권한 필요",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF)
                    )
                )
            },
            text = {
                Text(
                    text = "알림을 받으려면 설정에서 알림 권한을 허용해주세요.",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color(0xFFE1E1E1),
                        lineHeight = 24.sp
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSettingsDialog = false
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        })
                    }
                ) {
                    Text(
                        "설정으로 이동",
                        color = Color(0xFFFF6B6B)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSettingsDialog = false }
                ) {
                    Text(
                        "취소",
                        color = Color(0xFFCCCCCC)
                    )
                }
            },
            containerColor = Color(0xFF1A2847),
            shape = RoundedCornerShape(8.dp),
            properties = DialogProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true
            )
        )
    }

    CommonToggleItem(
        title = "데일리 명언 알림 설정",
        isEnabled = isEnabled,
        onToggleChange = { enabled ->
            if (currentUser == null) {
                viewModel.updateNotificationSettings(false)
                return@CommonToggleItem
            }

            if (enabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        viewModel.updateNotificationSettings(true)
                    } else {
                        // 권한이 없으면 설정으로 이동하는 다이얼로그 표시
                        showSettingsDialog = true
                    }
                } else {
                    viewModel.updateNotificationSettings(true)
                }
            } else {
                viewModel.updateNotificationSettings(false)
            }
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

    LaunchedEffect(showTimePickerDialog) {
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
            ).apply {
                setOnDismissListener { showTimePickerDialog = false }
                show()
            }
        }
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
    val shouldNavigateToLogin by viewModel.navigateToLogin.collectAsState()

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
    val shouldNavigateToQuote by viewModel.navigateToQuote.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                    text = "로그아웃하시면 데일리 명언 알림을 받지 못하실 거예요.\n그래도 로그아웃하시겠어요?",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color(0xFFE1E1E1),
                        lineHeight = 24.sp
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
                        "로그아웃",
                        color = Color(0xFFFF6B6B)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        "취소",
                        color = Color(0xFFCCCCCC)
                    )
                }
            },
            containerColor = Color(0xFF1A2847),
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
    val shouldNavigateToQuote by viewModel.navigateToQuote.collectAsState()
    val showWithdrawalDialog by viewModel.showWithdrawalDialog.collectAsState()

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

@Composable
fun TermsSettingItem() {
    val context = LocalContext.current

    CommonNavigationItem(
        title = "이용약관",
        onClick = {
            openUrl(
                context,
                "https://sites.google.com/view/passiondaily-1/%EC%84%9C%EB%B9%84%EC%8A%A4-%EC%9D%B4%EC%9A%A9-%EC%95%BD%EA%B4%80?authuser=0"
            )
        }
    )
}

@Composable
fun PrivacySettingItem() {
    val context = LocalContext.current

    CommonNavigationItem(
        title = "개인정보 처리방침",
        onClick = {
            openUrl(
                context,
                "https://sites.google.com/view/passiondaily-1/%EA%B0%9C%EC%9D%B8-%EC%A0%95%EB%B3%B4-%EC%B2%98%EB%A6%AC-%EB%B0%A9%EC%B9%A8?authuser=0"
            )
        }
    )
}

fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
    }
    context.startActivity(intent)
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

