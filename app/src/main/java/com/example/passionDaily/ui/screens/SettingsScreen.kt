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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passionDaily.R
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
        Column(
            modifier = Modifier
                .offset(x = 24.dp, y = 24.dp)
                .align(Alignment.TopStart)
                .clickable {  } // 이전 화면으로 가기 ()
        ) {
            BackButton()
        }

        Column(
            modifier = Modifier
                .offset(y = 24.dp)
                .align(Alignment.TopCenter)
        ) {
            SettingsText()
        }

        Spacer(modifier = Modifier.height(41.dp))

        Column {
            BlueBar("알림 설정")
            NotificationSetting()
            PromotionSetting()
        }

        Row(
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
fun SettingsText() {
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
fun BlueBar(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(color = Color(0xFF0E1C41))
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
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
fun NotificationSetting() {
    var checked by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 20.dp)
    ) {
        Text(
            text = "알림 설정",
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFFFF),
            )
        )
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
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
fun PromotionSetting() {

    var checked by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 20.dp)
    ) {
        Text(
            text = "알림 설정",
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFFFF),
            )
        )
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0E1C41),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}


