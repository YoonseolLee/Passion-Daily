package com.example.passionDaily.settings.presentation.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passionDaily.R
import com.example.passionDaily.constants.NavigationBarScreens
import com.example.passionDaily.settings.presentation.viewmodel.SettingsViewModel
import com.example.passionDaily.ui.component.BackButton
import com.example.passionDaily.ui.component.CommonNavigationBar

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentScreen: NavigationBarScreens,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScreenContent(
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings,
            currentScreen = currentScreen,
            onBack = onBack,
            onSendSuggestion = {
                settingsViewModel.sendSuggestion(context)
            },
        )
    }
}

@Composable
fun SettingsScreenContent(
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentScreen: NavigationBarScreens,
    onBack: () -> Unit,
    onSendSuggestion: () -> Unit,
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
            // 고객 지원
            SettingsCategoryHeader(text = stringResource(id = R.string.customer_support))
            SuggestionSettingItem(
                onSendSuggestion = onSendSuggestion
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
fun SuggestionSettingItem(
    onSendSuggestion: () -> Unit
) {
    CommonIconItem(
        title = stringResource(R.string.send_suggestion),
        icon = Icons.Filled.Email,
        onClick = onSendSuggestion
    )
}

@Composable
fun VersionInfoItem() {
    val context = LocalContext.current
    val unknownVersion = stringResource(R.string.unknown_version)

    val versionName = remember(context, unknownVersion) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: unknownVersion
        } catch (e: Exception) {
            unknownVersion
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
            contentDescription = stringResource(R.string.content_description_navigate_to_next),
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
            contentDescription = stringResource(R.string.content_description_icon_for, title),
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
fun SettingsCategoryHeader(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(color = Color(0xFF0E1C41))
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .testTag(stringResource(R.string.test_tag_category_header)),
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
        modifier = Modifier.testTag(stringResource(R.string.test_tag_settings_title))
    )
}