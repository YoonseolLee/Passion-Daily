package com.example.passionDaily.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passionDaily.R
import com.example.passionDaily.ui.screens.NavigationBarScreens

@Composable
fun CommonNavigationBar(
    currentScreen: NavigationBarScreens,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color(0xFF444444))
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationButton(
                unselectedIcon = painterResource(id = R.drawable.favorites_icon),
                selectedIcon = painterResource(id = R.drawable.favorites_icon_filled),
                text = stringResource(R.string.favorites),
                isSelected = currentScreen == NavigationBarScreens.FAVORITES,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 40.dp),
                onClick = onNavigateToFavorites
            )

            NavigationButton(
                unselectedIcon = painterResource(id = R.drawable.home_icon),
                selectedIcon = painterResource(id = R.drawable.home_icon_filled),
                text = stringResource(R.string.home),
                isSelected = currentScreen == NavigationBarScreens.QUOTE,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToQuote
            )

            NavigationButton(
                unselectedIcon = painterResource(id = R.drawable.settings_icon),
                selectedIcon = painterResource(id = R.drawable.settings_icon_filled),
                text = stringResource(R.string.settings),
                isSelected = currentScreen == NavigationBarScreens.SETTINGS,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 40.dp),
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun NavigationButton(
    unselectedIcon: Painter,
    selectedIcon: Painter,
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
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