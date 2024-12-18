package com.example.passionDaily.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.passionDaily.R
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.GrayScaleWhite
import com.example.passionDaily.ui.theme.Passion_DailyTheme
import com.example.passionDaily.ui.viewmodels.SharedQuoteViewModel
import com.example.passionDaily.util.Categories

@Composable
fun CategoryScreen(sharedQuoteViewModel: SharedQuoteViewModel = hiltViewModel()) {
    val categories = sharedQuoteViewModel.getCategories()
    CategoryScreenContent(categories)
}

@Composable
fun CategoryScreenContent(categories: List<String>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        Column(
            modifier = Modifier
                .offset(x = 24.dp, y = 24.dp)
                .align(Alignment.TopStart)
        ) {
            BackButton()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 24.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                Category()
            }
            Spacer(modifier = Modifier.height(26.dp))
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                CategorySelectionGuide()
            }
            Spacer(modifier = Modifier.height(28.dp))
            CategoryBoxes(categories)
        }
    }
}

@Composable
fun BackButton() {
    Image(
        painter = painterResource(id = R.drawable.clarity_arrow_line),
        contentDescription = "back_button"
    )
}

@Composable
fun Category() {
    Text(
        text = stringResource(id = R.string.category),
        style =
        TextStyle(
            fontSize = 24.sp,
            fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
            fontWeight = FontWeight(400),
            color = GrayScaleWhite,
        )
    )
}

@Composable
fun CategorySelectionGuide() {
    Text(
        text = stringResource(id = R.string.category_selection_guide),
        style =
        TextStyle(
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
            fontWeight = FontWeight(400),
            color = Color(0xFF999494),
        )
    )
}

@Composable
fun CategoryBoxes(categories: List<String>) {

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
    ) {
        items(categories.chunked(2)) { rowCategories ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowCategories.forEach { category ->
                    SingleCategoryBox(category = category)
                }

                if (rowCategories.size == 1) {
                    Spacer(modifier = Modifier.width(125.dp))
                }
            }
        }
    }
}


@Composable
fun SingleCategoryBox(
    category: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(125.dp)
            .height(125.dp)
            .background(color = Color(0xFFD9D9D9), shape = RoundedCornerShape(size = 16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category,
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
            )
        )
    }
}

@Preview
@Composable
fun CategoryScreenContentPreview() {
    Passion_DailyTheme {
        CategoryScreenContent(
            categories = Categories.values().map { it.toKorean() }
        )
    }
}