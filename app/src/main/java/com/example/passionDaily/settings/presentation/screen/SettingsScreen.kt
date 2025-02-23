package com.example.passionDaily.settings.presentation.screen

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import com.example.passionDaily.R
import com.example.passionDaily.settings.presentation.viewmodel.SettingsViewModel
import com.example.passionDaily.ui.component.CommonNavigationBar
import java.time.LocalTime
import android.provider.Settings
import android.Manifest
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.example.passionDaily.ui.component.BackButton
import com.example.passionDaily.constants.NavigationBarScreens
import com.example.passionDaily.ui.theme.PrimaryColor
import com.google.firebase.auth.FirebaseUser

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentScreen: NavigationBarScreens,
    onBack: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val currentUser by settingsViewModel.currentUser.collectAsState()
        val isNotificationEnabled by settingsViewModel.notificationEnabled.collectAsState()
        val notificationTime by settingsViewModel.notificationTime.collectAsState()
        val showWithdrawalDialog by settingsViewModel.showWithdrawalDialog.collectAsState()

        SettingsScreenContent(
            currentUser = currentUser,
            isNotificationEnabled = isNotificationEnabled,
            notificationTime = notificationTime,
            showWithdrawalDialog = showWithdrawalDialog,
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings,
            currentScreen = currentScreen,
            onBack = onBack,
            onUpdateNotificationSettings = { enabled ->
                settingsViewModel.updateNotificationSettings(enabled)
            },
            onUpdateNotificationTime = { time ->
                settingsViewModel.updateNotificationTime(time)
            },
            onLogout = {
                settingsViewModel.logOut(onNavigateToQuote)
            },
            onCreateEmailIntent = {
                settingsViewModel.createEmailIntent() ?: Intent()
            },
            onUpdateShowWithdrawalDialog = { show ->
                settingsViewModel.updateShowWithdrawalDialog(show)
            },
            onWithdrawUser = {
                settingsViewModel.withdrawUser(onNavigateToQuote, onNavigateToLogin)
            }
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
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("LoadingIndicator")
                )
            }
        }
    }
}

@Composable
fun SettingsScreenContent(
    currentUser: FirebaseUser?,
    isNotificationEnabled: Boolean,
    notificationTime: LocalTime?,
    showWithdrawalDialog: Boolean,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentScreen: NavigationBarScreens,
    onBack: () -> Unit,
    onUpdateNotificationSettings: (Boolean) -> Unit,
    onUpdateNotificationTime: (LocalTime) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onCreateEmailIntent: () -> Intent,
    onUpdateShowWithdrawalDialog: (Boolean) -> Unit,
    onWithdrawUser: () -> Unit,
) {
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
            NotificationSettingItem(
                isEnabled = isNotificationEnabled,
                currentUser = currentUser,
                onUpdateNotificationSettings = onUpdateNotificationSettings,
                onLogin = onLogin
            )
            NotificationTimeSettingItem(
                notificationTime = notificationTime,
                onUpdateNotificationTime = onUpdateNotificationTime,
                currentUser = currentUser,
            )

            // 프로필 설정
            SettingsCategoryHeader(text = stringResource(id = R.string.account_management))
            if (currentUser != null) {
                LogoutSettingItem(
                    onLogout = onLogout
                )
            } else {
                LoginSettingItem(
                    onLogin = onLogin
                )
            }

            // 고객 지원
            SettingsCategoryHeader(text = stringResource(id = R.string.customer_support))
            SuggestionSettingItem(
                onCreateEmailIntent = onCreateEmailIntent
            )
            WithdrawalSettingItem(
                showWithdrawalDialog = showWithdrawalDialog,
                onUpdateShowWithdrawalDialog = onUpdateShowWithdrawalDialog,
                onWithdrawUser = onWithdrawUser
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
fun NotificationSettingItem(
    isEnabled: Boolean,
    currentUser: FirebaseUser?,
    onUpdateNotificationSettings: (Boolean) -> Unit,
    onLogin: () -> Unit
) {
    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }

    NotificationPermissionDialog(
        showDialog = showSettingsDialog,
        onDismiss = { showSettingsDialog = false },
        onConfirm = {
            showSettingsDialog = false
            navigateToAppSettings(context)
        }
    )

    // 비로그인 상태면 항상 off, 클릭시 로그인으로 이동
    NotificationToggle(
        isEnabled = if (currentUser == null) false else isEnabled,
        currentUser = currentUser,
        onToggleChange = { enabled ->
            if (currentUser == null) {
                onLogin()
                return@NotificationToggle
            }
            handleNotificationToggle(
                enabled = enabled,
                context = context,
                currentUser = currentUser,
                onUpdateNotificationSettings = onUpdateNotificationSettings,
                showSettingsDialog = { showSettingsDialog = true }
            )
        }
    )
}

private fun handleNotificationToggle(
    enabled: Boolean,
    context: Context,
    currentUser: FirebaseUser?,
    onUpdateNotificationSettings: (Boolean) -> Unit,
    showSettingsDialog: () -> Unit
) {
    if (currentUser == null) {
        onUpdateNotificationSettings(false)
        return
    }

    if (!enabled) {
        onUpdateNotificationSettings(false)
        return
    }

    // Android 13(Tiramisu) 이상에서만 권한 체크
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            onUpdateNotificationSettings(true)
        } else {
            showSettingsDialog()
        }
    } else {
        onUpdateNotificationSettings(true)
    }
}

@Composable
fun NotificationTimeSettingItem(
    notificationTime: LocalTime?,
    onUpdateNotificationTime: (LocalTime) -> Unit,
    currentUser: FirebaseUser?
) {
    val context = LocalContext.current
    var showTimePickerDialog by rememberSaveable { mutableStateOf(false) }

    val displayTime = if (currentUser == null) {
        stringResource(R.string.default_notification_time)
    } else {
        notificationTime?.toString() ?: stringResource(R.string.default_notification_time)
    }

    CommonTextItem(
        title = stringResource(R.string.notification_time_setting),
        value = displayTime,
        onClick = {
            if (currentUser != null) {
                // Dialog를 직접 생성하고 show
                TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        val selectedTime = LocalTime.of(selectedHour, selectedMinute)
                        onUpdateNotificationTime(selectedTime)
                    },
                    notificationTime?.hour ?: 8,
                    notificationTime?.minute ?: 0,
                    true
                ).show()
            }
        }
    )
}

@Composable
fun LoginSettingItem(
    onLogin: () -> Unit
) {
    CommonNavigationItem(
        title = stringResource(R.string.login),
        onClick = onLogin
    )
}

@Composable
fun LogoutSettingItem(
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    LogoutConfirmationDialog(
        showDialog = showLogoutDialog,
        onConfirm = {
            onLogout()
            showLogoutDialog = false
        },
        onDismiss = { showLogoutDialog = false }
    )

    LogoutButton(
        onClick = { showLogoutDialog = true }
    )
}

@Composable
fun SuggestionSettingItem(
    onCreateEmailIntent: () -> Intent
) {
    val context = LocalContext.current

    CommonIconItem(
        title = stringResource(R.string.send_suggestion),
        icon = Icons.Filled.Email,
        onClick = {
            val intent = onCreateEmailIntent()
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    )
}

@Composable
fun WithdrawalSettingItem(
    showWithdrawalDialog: Boolean,
    onUpdateShowWithdrawalDialog: (Boolean) -> Unit,
    onWithdrawUser: () -> Unit
) {
    WithdrawalButton(
        onClick = {
            onUpdateShowWithdrawalDialog(true)
        }
    )

    if (showWithdrawalDialog) {
        AlertDialog(
            onDismissRequest = { onUpdateShowWithdrawalDialog(false) },
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
                        color = Color(0xFFE1E1E1),
                        lineHeight = 24.sp
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onWithdrawUser()
                        onUpdateShowWithdrawalDialog(false)
                    }
                ) {
                    Text(
                        stringResource(R.string.withdrawal),
                        color = Color(0xFFFF6B6B)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onUpdateShowWithdrawalDialog(false) }
                ) {
                    Text(
                        stringResource(R.string.no),
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
}

@Composable
private fun WithdrawalButton(onClick: () -> Unit) {
    CommonNavigationItem(
        title = stringResource(R.string.withdrawal),
        onClick = onClick
    )
}

@Composable
private fun LogoutConfirmationDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            LogoutDialogTitle()
        },
        text = {
            LogoutDialogMessage()
        },
        confirmButton = {
            LogoutConfirmButton(onClick = onConfirm)
        },
        dismissButton = {
            LogoutCancelButton(onClick = onDismiss)
        },
        containerColor = Color(0xFF1A2847),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun LogoutDialogTitle() {
    Text(
        text = stringResource(R.string.logout),
        style = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFFFFF)
        )
    )
}

@Composable
private fun LogoutDialogMessage() {
    Text(
        text = stringResource(R.string.logout_confirmation_message),
        style = TextStyle(
            fontSize = 15.sp,
            color = Color(0xFFE1E1E1),
            lineHeight = 24.sp
        )
    )
}

@Composable
private fun LogoutConfirmButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            stringResource(R.string.logout),
            color = Color(0xFFFF6B6B)
        )
    }
}

@Composable
private fun LogoutCancelButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            stringResource(R.string.cancel),
            color = Color(0xFFCCCCCC)
        )
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    CommonNavigationItem(
        title = stringResource(R.string.logout),
        onClick = onClick
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

@Composable
fun NotificationPermissionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
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
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.go_to_settings),
                    color = Color(0xFFFF6B6B)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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

@Composable
private fun NotificationToggle(
    isEnabled: Boolean,
    currentUser: FirebaseUser?,
    onToggleChange: (Boolean) -> Unit
) {
    CommonToggleItem(
        title = stringResource(R.string.daily_quote_notification_setting),
        isEnabled = isEnabled,
        onToggleChange = onToggleChange
    )
}

private fun navigateToAppSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    })
}

@Composable
fun SettingsCategoryHeader(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(color = Color(0xFF0E1C41))
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .testTag("CategoryHeader"),
        verticalAlignment = Alignment.CenterVertically
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
        ),
        modifier = Modifier.testTag("SettingsTitle")
    )
}

