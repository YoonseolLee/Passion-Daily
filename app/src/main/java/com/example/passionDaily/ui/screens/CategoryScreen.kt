package com.example.passionDaily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passionDaily.R
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.GrayScaleWhite
import com.example.passionDaily.ui.viewmodels.QuoteViewModel
import com.example.passionDaily.util.QuoteCategory

@Composable
fun CategoryScreen(
    quoteViewModel: QuoteViewModel,
    onNavigateToQuote: () -> Unit,
    onBack: () -> Unit,
) {
    val categories = quoteViewModel.getQuoteCategories()
    val selectedCategory by quoteViewModel.selectedQuoteCategory.collectAsState()

    CategoryScreenContent(
        categories = categories,
        selectedCategory = selectedCategory,
        onCategoryClicked = { category ->
            quoteViewModel.onCategorySelected(category)
            onNavigateToQuote()
        },
        onBack = onBack
    )
}

@Composable
fun CategoryScreenContent(
    categories: List<String>,
    selectedCategory: QuoteCategory?,
    onCategoryClicked: (QuoteCategory?) -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
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
                Category()
            }

            Box(
                modifier = Modifier.width(72.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 96.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CategorySelectionGuide()

            Spacer(modifier = Modifier.height(28.dp))

            CategoryBoxes(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategoryClicked = onCategoryClicked
            )
        }
    }
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
fun CategoryBoxes(
    categories: List<String>,
    selectedCategory: QuoteCategory?,
    onCategoryClicked: (QuoteCategory?) -> Unit
) {

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
                    SingleCategoryBox(
                        category = category,
                        selectedCategory = selectedCategory,
                        onCategoryClicked = onCategoryClicked
                    )
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
    selectedCategory: QuoteCategory?,
    onCategoryClicked: (QuoteCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentCategory = QuoteCategory.fromKoreanName(category)
    val isSelected = selectedCategory == currentCategory

    Box(
        modifier = modifier
            .width(125.dp)
            .height(125.dp)
            .background(
                color = if (isSelected) Color(0xFF9E9E9E) else Color(0xFFD9D9D9),
                shape = RoundedCornerShape(size = 16.dp)
            )
            .clickable { onCategoryClicked(currentCategory) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category,
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = if (isSelected) Color.White else Color(0xFF000000),
            )
        )
    }
}
