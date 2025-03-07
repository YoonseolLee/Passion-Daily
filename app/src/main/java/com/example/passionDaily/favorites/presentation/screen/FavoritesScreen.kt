package com.example.passionDaily.favorites.presentation.screen

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import androidx.core.view.WindowCompat
import com.example.passionDaily.R
import com.example.passionDaily.constants.NavigationBarScreens
import com.example.passionDaily.favorites.presentation.viewmodel.FavoritesViewModel
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import com.example.passionDaily.quote.presentation.components.BackgroundImage
import com.example.passionDaily.quote.presentation.components.Buttons
import com.example.passionDaily.quote.presentation.components.LeftArrow
import com.example.passionDaily.quote.presentation.components.QuoteAndPerson
import com.example.passionDaily.quote.presentation.components.RightArrow
import com.example.passionDaily.quote.presentation.components.toQuoteDisplay
import com.example.passionDaily.quote.presentation.viewmodel.QuoteViewModel
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.example.passionDaily.ui.component.AnimationSpecs
import com.example.passionDaily.ui.component.CommonNavigationBar

@Composable
fun FavoritesScreen(
    favoritesViewModel: FavoritesViewModel,
    quoteViewModel: QuoteViewModel,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentScreen: NavigationBarScreens,
) {
    val favoriteQuotes by favoritesViewModel.favoriteQuotes.collectAsState()
    val currentFavoriteQuote by favoritesViewModel.currentFavoriteQuote.collectAsState()
    val isFavoriteLoading by favoritesViewModel.isFavoriteLoading.collectAsState()
    val isFavoriteQuotesEmpty = remember {
        derivedStateOf { favoriteQuotes.isEmpty() }
    }.value
    var slideDirection by remember {
        mutableStateOf(AnimatedContentTransitionScope.SlideDirection.Start)
    }

    SetupNavigationBarColor()

    LaunchedEffect(currentScreen) {
        if (currentScreen == NavigationBarScreens.FAVORITES) {
            favoritesViewModel.loadFavorites()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 이미지
        currentFavoriteQuote?.let {
            BackgroundImage(imageUrl = it.imageUrl)
        }

        // 메인 콘텐츠 영역
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            when {
                isFavoriteLoading -> {
                    LoadingIndicator(Modifier.align(Alignment.Center))
                }
                isFavoriteQuotesEmpty -> {
                    EmptyFavoritesContent(Modifier.align(Alignment.Center))
                }
                else -> {
                    FavoritesContent(
                        favoriteQuotes = favoriteQuotes,
                        currentFavoriteQuote = currentFavoriteQuote,
                        slideDirection = slideDirection,
                        onPreviousClick = {
                            slideDirection = AnimatedContentTransitionScope.SlideDirection.End
                            favoritesViewModel.previousQuote()
                        },
                        onNextClick = {
                            slideDirection = AnimatedContentTransitionScope.SlideDirection.Start
                            favoritesViewModel.nextQuote()
                        },
                        quoteViewModel = quoteViewModel,
                        favoritesViewModel = favoritesViewModel
                    )
                }
            }
        }

        // 하단 네비게이션 바
        BottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentScreen = currentScreen,
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}

@Composable
private fun SetupNavigationBarColor() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        if (window != null) {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightNavigationBars = false
            }
            window.navigationBarColor = android.graphics.Color.BLACK
        }
        onDispose {}
    }
}

@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier
            .testTag(stringResource(R.string.test_tag_loading_indicator))
    )
}

@Composable
private fun EmptyFavoritesContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.sentiment_dissatisfied_icon),
            contentDescription = stringResource(R.string.content_description_no_favorites),
            contentScale = ContentScale.None
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_quotes_in_favorites),
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFFFF),
            )
        )
    }
}

@Composable
private fun FavoritesContent(
    favoriteQuotes: List<QuoteEntity>,
    currentFavoriteQuote: QuoteEntity?,
    slideDirection: AnimatedContentTransitionScope.SlideDirection,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    quoteViewModel: QuoteViewModel,
    favoritesViewModel: FavoritesViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 좌측 화살표
        NavigationArrow(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            isLeft = true,
            onClick = onPreviousClick,
            enabled = favoriteQuotes.size > 1
        )

        // 우측 화살표
        NavigationArrow(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            isLeft = false,
            onClick = onNextClick,
            enabled = favoriteQuotes.size > 1
        )

        // 중앙 인용구 영역
        QuoteContent(
            modifier = Modifier.fillMaxSize(),
            favoriteQuotes = favoriteQuotes,
            currentFavoriteQuote = currentFavoriteQuote,
            slideDirection = slideDirection
        )

        // 하단 버튼 영역
        QuoteButtons(
            modifier = Modifier
                .offset(y = -172.dp)
                .align(Alignment.BottomCenter),
            currentFavoriteQuote = currentFavoriteQuote,
            quoteViewModel = quoteViewModel,
            favoritesViewModel = favoritesViewModel
        )
    }
}

@Composable
private fun NavigationArrow(
    modifier: Modifier = Modifier,
    isLeft: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Box(modifier = modifier) {
        if (isLeft) {
            LeftArrow(
                onClick = onClick,
                enabled = enabled
            )
        } else {
            RightArrow(
                onClick = onClick,
                enabled = enabled
            )
        }
    }
}

@Composable
private fun QuoteContent(
    modifier: Modifier = Modifier,
    favoriteQuotes: List<QuoteEntity>,
    currentFavoriteQuote: QuoteEntity?,
    slideDirection: AnimatedContentTransitionScope.SlideDirection
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // 현재 인용구의 위치를 찾아 인덱스로 사용
        val currentIndex = remember(currentFavoriteQuote) {
            derivedStateOf {
                favoriteQuotes.indexOfFirst { quote ->
                    quote.categoryId == currentFavoriteQuote?.categoryId &&
                            quote.quoteId == currentFavoriteQuote?.quoteId
                }
            }
        }.value

        AnimatedQuote(
            currentIndex = currentIndex,
            favoriteQuotes = favoriteQuotes,
            slideDirection = slideDirection
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AnimatedQuote(
    currentIndex: Int,
    favoriteQuotes: List<QuoteEntity>,
    slideDirection: AnimatedContentTransitionScope.SlideDirection
) {
    AnimatedContent(
        targetState = currentIndex,
        transitionSpec = {
            val direction = slideDirection
            (slideIntoContainer(
                direction,
                animationSpec = AnimationSpecs.ContentAnimationSpec
            ) +
                    fadeIn(animationSpec = AnimationSpecs.FadeAnimationSpec)).togetherWith(
                slideOutOfContainer(
                    direction,
                    animationSpec = AnimationSpecs.ContentAnimationSpec
                ) +
                        fadeOut(animationSpec = AnimationSpecs.FadeAnimationSpec)
            )
        }
    ) { index ->
        if (index >= 0 && index < favoriteQuotes.size) {
            val quote = favoriteQuotes[index]
            QuoteAndPerson(
                quote = quote.text,
                author = quote.person
            )
        }
    }
}

@Composable
private fun QuoteButtons(
    modifier: Modifier = Modifier,
    currentFavoriteQuote: QuoteEntity?,
    quoteViewModel: QuoteViewModel,
    favoritesViewModel: FavoritesViewModel
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        currentFavoriteQuote?.let { quote ->
            val quoteCategory = remember(quote.categoryId) {
                QuoteCategory.fromCategoryId(quote.categoryId)
            }

            val quoteDisplay = remember(quote.quoteId, quote.categoryId) {
                quote.toQuoteDisplay()
            }

            Buttons(
                quoteViewModel = quoteViewModel,
                favoritesViewModel = favoritesViewModel,
                currentQuoteId = quote.quoteId,
                category = quoteCategory,
                quoteDisplay = quoteDisplay
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    currentScreen: NavigationBarScreens,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Row(
        modifier = modifier.navigationBarsPadding()
    ) {
        val screen = remember(currentScreen) {
            currentScreen
        }

        CommonNavigationBar(
            currentScreen = screen,
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}