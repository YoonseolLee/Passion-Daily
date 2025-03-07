package com.example.passionDaily.quotecategory.presentation.screen

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passionDaily.R
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.Dimens.backButtonContainerWidth
import com.example.passionDaily.ui.theme.Dimens.categoryBoxCornerRadius
import com.example.passionDaily.ui.theme.Dimens.categoryBoxSize
import com.example.passionDaily.ui.theme.Dimens.categoryRowSpacing
import com.example.passionDaily.ui.theme.Dimens.categorySpacing
import com.example.passionDaily.ui.theme.Dimens.categoryTextSize
import com.example.passionDaily.ui.theme.Dimens.guideTextSize
import com.example.passionDaily.ui.theme.Dimens.headlineTextSize
import com.example.passionDaily.ui.theme.Dimens.startPadding
import com.example.passionDaily.ui.theme.Dimens.topBarTopPadding
import com.example.passionDaily.ui.theme.GuideTextColor
import com.example.passionDaily.ui.theme.SelectedGray
import com.example.passionDaily.ui.theme.UnselectedGray
import com.example.passionDaily.quote.presentation.viewmodel.QuoteViewModel
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.ui.component.BackButton
import com.example.passionDaily.quotecategory.model.QuoteCategory

@Composable
fun CategoryScreen(
    quoteViewModel: QuoteViewModel,
    quoteStateHolder: QuoteStateHolder,
    onNavigateToQuote: () -> Unit,
    onBack: () -> Unit,
) {
    val categories by quoteStateHolder.categories.collectAsState()
    val selectedCategory by quoteStateHolder.selectedQuoteCategory.collectAsState(
        initial = QuoteCategory.EFFORT
    )

    CategoryScreenContent(
        categories = categories,
        selectedCategory = selectedCategory,
        onCategoryClicked = { category ->
            if (category != null) {
                quoteViewModel.onCategorySelected(category)
            }
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar(onBack = onBack)

            CategorySelectionContent(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategoryClicked = onCategoryClicked
            )
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    val backContentDescription = stringResource(R.string.content_description_back)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(backButtonContainerWidth)
                .padding(start = startPadding)
        ) {
            BackButton(
                onBack = onBack,
                modifier = Modifier.semantics {
                    contentDescription = backContentDescription
                }
            )
        }

        Text(
            text = stringResource(id = R.string.category_kor),
            style = TextStyle(
                fontSize = headlineTextSize,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                fontWeight = FontWeight(400),
                color = Color.White,
            ),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(backButtonContainerWidth))
    }
}

@Composable
private fun CategorySelectionContent(
    categories: List<String>,
    selectedCategory: QuoteCategory?,
    onCategoryClicked: (QuoteCategory?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topBarTopPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.category_selection_guide),
            style = TextStyle(
                fontSize = guideTextSize,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                fontWeight = FontWeight(400),
                color = GuideTextColor,
            )
        )

        Spacer(modifier = Modifier.height(categorySpacing))

        CategoryBoxes(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategoryClicked = onCategoryClicked
        )
    }
}

@Composable
private fun CategoryBoxes(
    categories: List<String>,
    selectedCategory: QuoteCategory?,
    onCategoryClicked: (QuoteCategory?) -> Unit
) {
    val chunkedCategories = remember(categories) {
        categories.chunked(2)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(startPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
    ) {
        items(chunkedCategories) { rowCategories ->
            CategoryRow(
                categories = rowCategories,
                selectedCategory = selectedCategory,
                onCategoryClicked = onCategoryClicked
            )
        }
    }
}

@Composable
private fun CategoryRow(
    categories: List<String>,
    selectedCategory: QuoteCategory?,
    onCategoryClicked: (QuoteCategory?) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(categoryRowSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { category ->
            SingleCategoryBox(
                category = category,
                selectedCategory = selectedCategory,
                onCategoryClicked = onCategoryClicked
            )
        }

        // 카테고리가 홀수 개일 경우 빈 공간을 추가하여 정렬을 맞춘다.
        if (categories.size == 1) {
            Spacer(modifier = Modifier.width(categoryBoxSize))
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
    val currentCategory = remember(category) {
        try {
            QuoteCategory.fromKoreanName(category)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    val isSelected = selectedCategory == currentCategory
    val selectedText = if (isSelected) stringResource(id = R.string.content_description_selected) else ""
    val contentDescriptionText = stringResource(
        id = R.string.content_description_category_format,
        category,
        selectedText
    )

    Box(
        modifier = modifier
            .width(categoryBoxSize)
            .height(categoryBoxSize)
            .background(
                color = if (isSelected) SelectedGray else UnselectedGray,
                shape = RoundedCornerShape(size = categoryBoxCornerRadius)
            )
            .clickable(
                enabled = currentCategory != null,
                onClick = { onCategoryClicked(currentCategory) }
            )
            .semantics {
                contentDescription = contentDescriptionText
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category,
            style = TextStyle(
                fontSize = categoryTextSize,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = if (isSelected) Color.White else Color.Black,
            )
        )
    }
}