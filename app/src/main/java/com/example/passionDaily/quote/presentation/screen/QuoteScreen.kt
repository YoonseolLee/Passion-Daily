package com.example.passionDaily.quote.presentation.screen

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.passionDaily.R
import com.example.passionDaily.constants.NavigationBarScreens
import com.example.passionDaily.favorites.presentation.viewmodel.FavoritesViewModel
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quote.presentation.components.BackgroundImage
import com.example.passionDaily.quote.presentation.components.Buttons
import com.example.passionDaily.quote.presentation.components.CategorySelectionButton
import com.example.passionDaily.quote.presentation.components.LeftArrow
import com.example.passionDaily.quote.presentation.components.QuoteAndPerson
import com.example.passionDaily.quote.presentation.components.RightArrow
import com.example.passionDaily.quote.presentation.components.toQuoteDisplay
import com.example.passionDaily.quote.presentation.viewmodel.QuoteViewModel
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.example.passionDaily.ui.component.AnimationSpecs.ContentAnimationSpec
import com.example.passionDaily.ui.component.AnimationSpecs.FadeAnimationSpec
import com.example.passionDaily.ui.component.CommonNavigationBar
import com.example.passionDaily.ui.theme.PrimaryColor

@Composable
fun QuoteScreen(
    favoritesViewModel: FavoritesViewModel,
    quoteViewModel: QuoteViewModel,
    quoteStateHolder: QuoteStateHolder,
    onNavigateToCategory: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentScreen: NavigationBarScreens,
) {
    val selectedCategory by quoteStateHolder.selectedQuoteCategory.collectAsState()
    val quotes by quoteStateHolder.quotes.collectAsState()
    val isQuoteLoading by quoteStateHolder.isQuoteLoading.collectAsState()
    val currentQuote by quoteViewModel.currentQuote.collectAsState()

    var slideDirection by remember {
        mutableStateOf(AnimatedContentTransitionScope.SlideDirection.Start)
    }

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

    val backgroundImageUrl = remember {
        derivedStateOf {
            currentQuote?.imageUrl
        }
    }.value

    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 이미지는 URL이 있을 때만 표시
        backgroundImageUrl?.let {
            BackgroundImage(imageUrl = it)
        }

        Row(
            modifier = Modifier
                .offset(y = 110.dp)
                .align(Alignment.TopCenter)
        ) {
            // remember로 selectedCategory가 변경될 때만 재구성
            val buttonCategory = remember(selectedCategory) {
                selectedCategory
            }

            CategorySelectionButton(
                onCategoryClicked = onNavigateToCategory,
                selectedCategory = buttonCategory,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            val showLoadingIndicator = remember {
                derivedStateOf { isQuoteLoading && quotes.isEmpty() }
            }.value

            val showEmptyState = remember {
                derivedStateOf { !isQuoteLoading && quotes.isEmpty() }
            }.value

            if (showLoadingIndicator) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .testTag(stringResource(id = R.string.test_tag_loading_indicator))
                        .align(Alignment.Center),
                    color = PrimaryColor
                )
            } else if (showEmptyState) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.unable_to_load_data),
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            quoteViewModel.loadInitialQuotes()
                        },
                        colors = ButtonDefaults.buttonColors(PrimaryColor)
                    ) {
                        Text(stringResource(id = R.string.try_again), color = Color.White)
                    }
                }
            } else {
                val arrowsEnabled = remember {
                    derivedStateOf { !isQuoteLoading }
                }.value

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                ) {
                    LeftArrow(
                        onClick = {
                            if (arrowsEnabled) {
                                slideDirection = AnimatedContentTransitionScope.SlideDirection.End
                                quoteViewModel.previousQuote()
                            }
                        },
                        enabled = arrowsEnabled
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                ) {
                    RightArrow(
                        onClick = {
                            if (arrowsEnabled) {
                                slideDirection = AnimatedContentTransitionScope.SlideDirection.Start
                                quoteViewModel.nextQuote()
                            }
                        },
                        enabled = arrowsEnabled
                    )
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

                    val currentQuoteId = remember {
                        derivedStateOf {
                            currentQuote?.id ?: ""
                        }
                    }.value

                    AnimatedContent(
                        targetState = currentQuoteId,
                        transitionSpec = {
                            (slideIntoContainer(
                                slideDirection,
                                animationSpec = ContentAnimationSpec
                            ) +
                                    fadeIn(animationSpec = FadeAnimationSpec)).togetherWith(
                                slideOutOfContainer(
                                    slideDirection,
                                    animationSpec = ContentAnimationSpec
                                ) +
                                        fadeOut(animationSpec = FadeAnimationSpec)
                            )
                        }
                    ) { quoteId ->
                        val displayedQuote = remember(quoteId, quotes) {
                            quotes.find { it.id == quoteId } ?: currentQuote
                        }

                        key(displayedQuote?.id) {
                            displayedQuote?.let {
                                QuoteAndPerson(
                                    quote = it.text,
                                    author = it.person
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                // 하단 버튼
                Row(
                    modifier = Modifier
                        .offset(y = -172.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val buttonQuote = remember(currentQuote?.id) {
                        currentQuote
                    }

                    val buttonCategory = remember(selectedCategory) {
                        selectedCategory
                    }

                    buttonQuote?.let { quote ->
                        val quoteDisplay = remember(quote.id) {
                            quote.toQuoteDisplay()
                        }

                        Buttons(
                            quoteViewModel = quoteViewModel,
                            favoritesViewModel = favoritesViewModel,
                            currentQuoteId = quote.id,
                            category = buttonCategory,
                            quoteDisplay = quoteDisplay
                        )
                    }
                }
            }
        }

        // 하단 네비게이션 바
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
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
}