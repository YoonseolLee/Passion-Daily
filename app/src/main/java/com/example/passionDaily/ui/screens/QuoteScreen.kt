package com.example.passionDaily.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.Passion_DailyTheme
import com.example.passionDaily.ui.viewmodels.FakeQuoteViewModel
import com.example.passionDaily.ui.viewmodels.QuoteViewModelInterface
import com.example.passionDaily.ui.viewmodels.SharedQuoteViewModel

@Composable
fun QuoteScreen(
    sharedQuoteViewModel: SharedQuoteViewModel = hiltViewModel(),
    onNavigateToCategory: () -> Unit,
//    onNavigateToFavorite: () -> Unit,
//    onNavigateToSettings: () -> Unit,
) {
    val selectedCategory by sharedQuoteViewModel.selectedQuoteCategory.collectAsState()

    QuoteScreenContent(
        sharedQuoteViewModel,
        onCategoryClicked = onNavigateToCategory
    )
}

@Composable
fun QuoteScreenContent(
    sharedQuoteViewModel: QuoteViewModelInterface,
    onCategoryClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // TODO: 추후 이미지로 변경
            .background(BlackBackground)
    ) {
        Row(
            modifier = Modifier
                .offset(y = 110.dp)
                .align(Alignment.TopCenter)
        ) {
            CategorySelectionButton(
                onCategoryClicked = onCategoryClicked
            )
        }

        Column(
            modifier = Modifier
                .offset(y = 277.dp)
                .align(Alignment.TopCenter)
        ) {
            QuoteAndPerson()
        }

        Row(
            modifier = Modifier
                .offset(y = -168.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Buttons(sharedQuoteViewModel)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            NavigationBar()
        }
    }
}


@Composable
fun BackgroundPhoto() {
//    Image(
//        painter = painterResource(id = R.drawable.image1),
//        contentDescription = "image description",
//        contentScale = ContentScale.FillBounds
//    )
}

@Composable
fun CategorySelectionButton(
    onCategoryClicked: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(size = 999.dp)
            )
            .padding(start = 18.dp, top = 8.dp, bottom = 8.dp, end = 16.dp)
            .clickable { onCategoryClicked() }
    ) {
        Text(
            text = "창의력",
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFFFF),
            )
        )
        Image(
            painter = painterResource(id = R.drawable.simple_arrow),
            contentDescription = "navigation to category screen",
            contentScale = ContentScale.None
        )
    }
}

@Composable
fun QuoteAndPerson(
    quote: String = "노력해라",
    author: String = "사람 이름"
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, // 중앙 정렬 추가
        modifier = Modifier.fillMaxWidth() // 전체 너비 사용
    ) {
        Text(
            text = quote,
            style = TextStyle(
                fontSize = 20.sp,
                lineHeight = 36.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFFFF),
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(34.dp))
        Text(
            text = "-$author-",
            style = TextStyle(
                fontSize = 20.sp,
                lineHeight = 36.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF929292),
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun Buttons(sharedQuoteViewModel: QuoteViewModelInterface) {
    ShareButton(sharedQuoteViewModel)
    Spacer(modifier = Modifier.width(57.dp))
    AddToFavoritesButton()
}

@Composable
fun ShareButton(sharedQuoteViewModel: QuoteViewModelInterface) {
    val context = LocalContext.current // 현재 컨텍스트 가져오기

    Image(
        painter = painterResource(id = R.drawable.share_icon),
        contentDescription = "share icon",
        contentScale = ContentScale.None,
        modifier = Modifier.clickable {
            sharedQuoteViewModel.shareText(context, "공유 텍스트")
        }
    )
}


@Composable
fun AddToFavoritesButton(
    modifier: Modifier = Modifier,
    onFavoriteToggle: (Boolean) -> Unit = {}
) {
    var isFavorite by remember { mutableStateOf(false) }

    Image(
        painter = painterResource(id = R.drawable.add_to_favorites_icon),
        contentDescription = "add to favorites icon",
        contentScale = ContentScale.None,
        modifier = modifier.clickable {
            isFavorite = !isFavorite
            onFavoriteToggle(isFavorite)
        },
        colorFilter = if (isFavorite) ColorFilter.tint(Color.White) else null
    )
}

@Composable
fun NavigationBar(
    onTabSelected: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("home") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFF444444))
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center, // Center로 변경
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationButton(
                unselectedIcon = painterResource(id = R.drawable.favorites_icon),
                selectedIcon = painterResource(id = R.drawable.favorites_icon_filled),
                text = "즐겨찾기",
                isSelected = selectedTab == "favorites",
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 40.dp),
                onClick = {
                    selectedTab = "favorites"
                }
            )

            NavigationButton(
                unselectedIcon = painterResource(id = R.drawable.home_icon),
                selectedIcon = painterResource(id = R.drawable.home_icon_filled),
                text = "홈",
                isSelected = selectedTab == "home",
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedTab = "home"
                }
            )

            NavigationButton(
                unselectedIcon = painterResource(id = R.drawable.settings_icon),
                selectedIcon = painterResource(id = R.drawable.settings_icon_filled),
                text = "설정",
                isSelected = selectedTab == "settings",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 40.dp),
                onClick = {
                    selectedTab = "settings"
                }
            )
        }
    }
}

@Composable
fun NavigationButton(
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
            .clickable(onClick = onClick)
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
fun QuoteScreenContentPreview() {
    Passion_DailyTheme {
        QuoteScreenContent(
            sharedQuoteViewModel = FakeQuoteViewModel(),
            onCategoryClicked = {}
        )
    }
}
