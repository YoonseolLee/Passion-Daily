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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.res.stringResource
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
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings,
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
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
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
            SettingsCategoryHeader(text = stringResource(id = R.string.notification_settings))
            NotificationSettingItem(viewModel)
            NotificationTimeSettingItem(viewModel)

            // 프로필 설정
            SettingsCategoryHeader(text = stringResource(id = R.string.account_management))
            if (currentUser != null) {
                LogoutSettingItem(viewModel, onNavigateToQuote = onNavigateToQuote)
            } else {
                LoginSettingItem(viewModel, onNavigateToLogin = onNavigateToLogin)
            }

            // 고객 지원
            SettingsCategoryHeader(text = stringResource(id = R.string.customer_support))
            SuggestionSettingItem(viewModel)
            WithdrawalSettingItem(
                viewModel,
                onNavigateToQuote = onNavigateToQuote,
                onNavigateToLogin = onNavigateToLogin
            )
            VersionInfoItem()

            // 약관 및 개인정보
            SettingsCategoryHeader(text = stringResource(id = R.string.terms_and_privacy))
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
                onNavigateToFavorites = onNavigateToFavorites,
                onNavigateToQuote = onNavigateToQuote,
                onNavigateToSettings = onNavigateToSettings
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
        text = stringResource(R.string.settings),
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
                    text = stringResource(R.string.notification_permission_needed),
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF)
                    )
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.notification_permission_message),
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
                        stringResource(R.string.go_to_settings),
                        color = Color(0xFFFF6B6B)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSettingsDialog = false }
                ) {
                    Text(
                        stringResource(R.string.cancel),
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
        title = stringResource(R.string.daily_quote_notification_setting),
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
        title = stringResource(R.string.notification_time_setting),
        value = notificationTime?.toString() ?: stringResource(R.string.default_notification_time),
        onClick = { showTimePickerDialog = true }
    )
}

@Composable
fun LoginSettingItem(
    viewModel: SettingsViewModel,
    onNavigateToLogin: () -> Unit
) {
    CommonNavigationItem(
        title = stringResource(R.string.login),
        onClick = { viewModel.logIn(onNavigateToLogin) }
    )
}

@Composable
fun LogoutSettingItem(
    viewModel: SettingsViewModel,
    onNavigateToQuote: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.logout),
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF)
                    )
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.logout_confirmation_message),
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
                        viewModel.logOut(onNavigateToQuote)
                        showLogoutDialog = false
                    }
                ) {
                    Text(
                        stringResource(R.string.logout),
                        color = Color(0xFFFF6B6B)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        stringResource(R.string.cancel),
                        color = Color(0xFFCCCCCC)
                    )
                }
            },
            containerColor = Color(0xFF1A2847),
            shape = RoundedCornerShape(8.dp)
        )
    }

    CommonNavigationItem(
        title = stringResource(R.string.logout),
        onClick = { showLogoutDialog = true }
    )
}

// 고객 지원 항목들
@Composable
fun SuggestionSettingItem(
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current

    CommonIconItem(
        title = stringResource(R.string.send_suggestion),
        icon = Icons.Filled.Email,
        onClick = {
            val intent = viewModel.createEmailIntent()
            context.startActivity(intent)
        }
    )
}

@Composable
fun WithdrawalSettingItem(
    viewModel: SettingsViewModel,
    onNavigateToQuote: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val showWithdrawalDialog by viewModel.showWithdrawalDialog.collectAsState()

    if (showWithdrawalDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.updateShowWithdrawalDialog(false) },
            title = {
                Text(
                    text = stringResource(R.string.withdrawal),
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF)
                    )
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.withdrawal_confirmation_message),
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color(0xFFFFFFFF)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.withdrawUser(onNavigateToQuote, onNavigateToLogin)
                        viewModel.updateShowWithdrawalDialog(false)
                    }
                ) {
                    Text(
                        stringResource(R.string.yes),
                        color = Color(0xFFFFFFFF)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.updateShowWithdrawalDialog(false) }
                ) {
                    Text(
                        stringResource(R.string.no),
                        color = Color(0xFFFFFFFF)
                    )
                }
            },
            containerColor = Color(0xFF0E1C41),
            shape = RoundedCornerShape(8.dp)
        )
    }

    CommonNavigationItem(
        title = stringResource(R.string.withdrawal),
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
        title = stringResource(R.string.version_info),
        value = versionName,
        onClick = {}
    )
}

@Composable
fun TermsSettingItem() {
    val context = LocalContext.current
    val termsUrl = stringResource(R.string.terms_of_service_url)
    val termsTitle = stringResource(R.string.terms_of_service)

    CommonNavigationItem(
        title = termsTitle,
        onClick = {
            openUrl(context, termsUrl)
        }
    )
}

@Composable
fun PrivacySettingItem() {
    val context = LocalContext.current
    val privacyUrl = stringResource(R.string.privacy_policy_url)
    val privacyTitle = stringResource(R.string.privacy_policy)

    CommonNavigationItem(
        title = privacyTitle,
        onClick = {
            openUrl(context, privacyUrl)
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

