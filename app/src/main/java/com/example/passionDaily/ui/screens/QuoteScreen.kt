package com.example.passionDaily.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.passionDaily.R
import com.example.passionDaily.ui.viewmodels.SharedQuoteViewModel

@Composable
fun QuoteScreen(sharedQuoteViewModel: SharedQuoteViewModel = hiltViewModel()) {
    QuoteScreenContent()
}

@Composable
fun QuoteScreenContent() {

}

@Composable
fun BackgroundPhoto() {

}

@Composable
fun QuoteText() {

}

@Composable
fun PersonName() {

}

@Composable
fun SharedButton() {

}

@Composable
fun AddToFavoritesButton() {

}

@Composable
fun NavigationBar(
    onTabSelected: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf("home") }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFF444444))
            .padding(start = 40.dp, top = 16.dp, end = 40.dp, bottom = 16.dp)
    ) {
        NavigationButton(
            unselectedIcon = painterResource(id = R.drawable.favorites_icon),
            selectedIcon = painterResource(id = R.drawable.favorites_icon_filled),
            text = "즐겨찾기",
            isSelected = selectedTab == "favorites",
            onClick = {
                selectedTab = "favorites"
                onTabSelected("favorites")
            }
        )

        NavigationButton(
            unselectedIcon = painterResource(id = R.drawable.home_icon),
            selectedIcon = painterResource(id = R.drawable.home_icon_filled),
            text = "홈",
            isSelected = selectedTab == "home",
            onClick = {
                selectedTab = "home"
                onTabSelected("home")
            }
        )

        NavigationButton(
            unselectedIcon = painterResource(id = R.drawable.settings_icon),
            selectedIcon = painterResource(id = R.drawable.settings_icon_filled),
            text = "설정",
            isSelected = selectedTab == "settings",
            onClick = {
                selectedTab = "settings"
                onTabSelected("settings")
            }
        )
    }
}

@Composable
fun NavigationButton(
    unselectedIcon: Painter,
    selectedIcon: Painter,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Image(
            painter = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = text,
            contentScale = ContentScale.None
        )
        Text(
            text = text,
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = if (isSelected) Color.White else Color.Gray,
                textAlign = TextAlign.Center,
            )
        )
    }
}

@Preview
@Composable
fun NavigationBarPreview() {
    NavigationBar(onTabSelected = {})
}
