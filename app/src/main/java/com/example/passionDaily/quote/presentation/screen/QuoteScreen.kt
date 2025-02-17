package com.example.passionDaily.quote.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import com.example.passionDaily.quote.presentation.components.BackgroundImage
import com.example.passionDaily.quote.presentation.components.Buttons
import com.example.passionDaily.quote.presentation.components.CategorySelectionButton
import com.example.passionDaily.quote.presentation.components.LeftArrow
import com.example.passionDaily.quote.presentation.components.QuoteAndPerson
import com.example.passionDaily.quote.presentation.components.RightArrow
import com.example.passionDaily.quote.presentation.components.toQuoteDisplay
import com.example.passionDaily.ui.theme.PrimaryColor
import com.example.passionDaily.favorites.presentation.viewmodel.FavoritesViewModel
import com.example.passionDaily.quote.presentation.viewmodel.QuoteViewModel
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.constants.NavigationBarScreens
import com.example.passionDaily.ui.component.CommonNavigationBar
import android.app.Activity
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

@Composable
fun QuoteScreen(
    favoritesViewModel: FavoritesViewModel,
    quoteViewModel: QuoteViewModel,
    quoteStateHolder: QuoteStateHolder,
    onNavigateToCategory: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    currentScreen: NavigationBarScreens,
) {
    val selectedCategory by quoteStateHolder.selectedQuoteCategory.collectAsState()
    val currentQuote by quoteViewModel.currentQuote.collectAsState()
    val quotes by quoteStateHolder.quotes.collectAsState()
    val isQuoteLoading by quoteStateHolder.isQuoteLoading.collectAsState()

    var slideDirection by remember { mutableStateOf(AnimatedContentTransitionScope.SlideDirection.Start) }

    // 시스템 네비게이션 바 색상 설정
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        if (window != null) {
            // WindowCompat 사용
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightNavigationBars = false
            }
            window.navigationBarColor = android.graphics.Color.BLACK
        }

        onDispose {}
    }

    LaunchedEffect(selectedCategory) {
        if (quotes.isEmpty()) {
            quoteViewModel.loadQuotes(selectedCategory)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 백그라운드 이미지는 현재 명언이 있을 때만 표시
        currentQuote?.let {
            BackgroundImage(imageUrl = it.imageUrl)
        }

        // 로딩 중이거나 명언이 없을 때도 카테고리 선택 버튼 표시
        Row(
            modifier = Modifier
                .offset(y = 110.dp)
                .align(Alignment.TopCenter)
        ) {
            CategorySelectionButton(
                onCategoryClicked = onNavigateToCategory,
                selectedCategory = selectedCategory,
            )
        }

        // 메인 콘텐츠 영역
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)  // Navigation Bar 공간 확보
        ) {
            if (isQuoteLoading || currentQuote == null) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .testTag("LoadingIndicator")
                        .align(Alignment.Center),
                    color = PrimaryColor
                )
            } else {
                // 화살표 버튼
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                ) {
                    LeftArrow(onClick = {
                        slideDirection = AnimatedContentTransitionScope.SlideDirection.End
                        quoteViewModel.previousQuote()
                    })
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                ) {
                    RightArrow(onClick = {
                        slideDirection = AnimatedContentTransitionScope.SlideDirection.Start
                        quoteViewModel.nextQuote()
                    })
                }

                // 명언 표시
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    AnimatedContent(
                        targetState = currentQuote,
                        transitionSpec = {
                            val direction = slideDirection
                            (slideIntoContainer(direction, animationSpec = ContentAnimationSpec) +
                                    fadeIn(animationSpec = FadeAnimationSpec)).togetherWith(
                                slideOutOfContainer(
                                    direction,
                                    animationSpec = ContentAnimationSpec
                                ) +
                                        fadeOut(animationSpec = FadeAnimationSpec)
                            )
                        }
                    ) { quote ->
                        quote?.let {
                            QuoteAndPerson(
                                quote = it.text,
                                author = it.person
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                // 버튼들
                Row(
                    modifier = Modifier
                        .offset(y = -172.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    currentQuote?.let { quote ->
                        Buttons(
                            quoteViewModel = quoteViewModel,
                            favoritesViewModel = favoritesViewModel,
                            currentQuoteId = quote.id,
                            category = selectedCategory,
                            onRequireLogin = onNavigateToLogin,
                            quoteDisplay = quote.toQuoteDisplay()
                        )
                    }
                }
            }
        }

        // Navigation Bar는 항상 표시
        Row(
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

private val ContentAnimationSpec = tween<IntOffset>(
    durationMillis = 400,
    easing = FastOutSlowInEasing
)

private val FadeAnimationSpec = tween<Float>(
    durationMillis = 400,
    easing = LinearOutSlowInEasing
)