package com.example.passionDaily.quote.presentation.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.passionDaily.R
import com.example.passionDaily.favorites.presentation.viewmodel.FavoritesViewModel
import com.example.passionDaily.quote.presentation.viewmodel.QuoteViewModel
import com.example.passionDaily.quotecategory.model.QuoteCategory
import kotlinx.coroutines.launch

@Composable
fun BackgroundImage(imageUrl: String?) {
    imageUrl?.let {
        AsyncImage(
            model = it,
            contentDescription = stringResource(id = R.string.content_description_background_image),
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ArrowButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    Box(
        modifier = Modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = alpha
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
    ) {
        content()
    }
}

@Composable
fun LeftArrow(onClick: () -> Unit, enabled: Boolean = true) {
    ArrowButton(onClick = onClick) {
        Image(
            painter = painterResource(id = R.drawable.left_arrow),
            contentDescription = stringResource(id = R.string.content_description_left_arrow),
            contentScale = ContentScale.None,
            modifier = Modifier.testTag(stringResource(id = R.string.test_tag_left_arrow))
        )
    }
}

@Composable
fun RightArrow(onClick: () -> Unit, enabled: Boolean = true) {
    ArrowButton(onClick = onClick) {
        Image(
            painter = painterResource(id = R.drawable.right_arrow),
            contentDescription = stringResource(id = R.string.content_description_right_arrow),
            contentScale = ContentScale.None,
            modifier = Modifier.testTag(stringResource(id = R.string.test_tag_right_arrow))
        )
    }
}

@Composable
fun CategorySelectionButton(
    onCategoryClicked: () -> Unit,
    selectedCategory: QuoteCategory?
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
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCategoryClicked() }
    ) {
        Text(
            text = selectedCategory?.koreanName ?: stringResource(id = R.string.select_category),
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFFFFFFF),
            )
        )
        Image(
            painter = painterResource(id = R.drawable.simple_arrow),
            contentDescription = stringResource(id = R.string.content_description_navigate_to_category),
            contentScale = ContentScale.None
        )
    }
}

@Composable
fun QuoteAndPerson(
    quote: String,
    author: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .align(Alignment.Center)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = quote,
                    style = TextStyle(
                        fontSize = 24.sp,
                        lineHeight = 40.8.sp,
                        fontFamily = FontFamily(Font(R.font.yeonsung_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFFFFFF),
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.92.sp,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.quote_author_format, author),
                    style = TextStyle(
                        fontSize = 22.sp,
                        lineHeight = 39.6.sp,
                        fontFamily = FontFamily(Font(R.font.yeonsung_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFFFFFF),
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }
    }
}

@Composable
fun Buttons(
    quoteViewModel: QuoteViewModel,
    favoritesViewModel: FavoritesViewModel,
    currentQuoteId: String,
    category: QuoteCategory,
    quoteDisplay: QuoteDisplay
) {
    ShareButton(
        quoteViewModel = quoteViewModel,
        currentQuoteId = currentQuoteId,
        category = category,
        quoteDisplay = quoteDisplay
    )
    Spacer(modifier = Modifier.width(57.dp))
    AddToFavoritesButton(
        favoritesViewModel = favoritesViewModel,
        currentQuoteId = currentQuoteId,
        category = category,
    )
}

@Composable
fun ShareButton(
    quoteViewModel: QuoteViewModel,
    currentQuoteId: String,
    category: QuoteCategory,
    quoteDisplay: QuoteDisplay
) {
    val context = LocalContext.current

    Image(
        painter = painterResource(id = R.drawable.share_icon),
        contentDescription = stringResource(id = R.string.content_description_share_icon),
        contentScale = ContentScale.None,
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {

            quoteViewModel.shareQuote(
                context = context,
                imageUrl = quoteDisplay.imageUrl,
                quoteText = quoteDisplay.text,
                author = quoteDisplay.person
            )
            quoteViewModel.incrementShareCount(currentQuoteId, category)
        }
    )
}

@Composable
fun AddToFavoritesButton(
    favoritesViewModel: FavoritesViewModel,
    currentQuoteId: String,
    category: QuoteCategory,
) {
    val coroutineScope = rememberCoroutineScope()
    val categoryId = category?.categoryId ?: return

    val isFavorite by favoritesViewModel.isFavorite(currentQuoteId, categoryId)
        .collectAsState(initial = false)

    var isAnimating by remember { mutableStateOf(false) }
    var localIsFavorite by remember { mutableStateOf(isFavorite) }

    LaunchedEffect(isFavorite) {
        localIsFavorite = isFavorite
    }

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { isAnimating = false }
    )

    val rotation by animateFloatAsState(
        targetValue = if (isAnimating) 20f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val iconResource = if (isFavorite) {
        R.drawable.remove_from_favorites_icon
    } else {
        R.drawable.add_to_favorites_icon
    }

    val contentDescription = if (isFavorite) {
        stringResource(id = R.string.content_description_remove_from_favorites)
    } else {
        stringResource(id = R.string.content_description_add_to_favorites)
    }

    Image(
        painter = painterResource(id = iconResource),
        contentDescription = contentDescription,
        contentScale = ContentScale.None,
        modifier = Modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                rotationZ = rotation
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                isAnimating = true

                localIsFavorite = !localIsFavorite

                coroutineScope.launch {
                    try {
                        if (!localIsFavorite) {
                            favoritesViewModel.removeFavorite(currentQuoteId, categoryId)
                        } else {
                            favoritesViewModel.addFavorite(currentQuoteId)
                        }
                    } catch (e: Exception) {
                        // 실패 시 UI 상태를 원래대로 복구
                        localIsFavorite = !localIsFavorite
                    }
                }
            }
    )
}