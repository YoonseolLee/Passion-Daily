package com.example.passionDaily.quote.presentation.components

import android.util.Log
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.passionDaily.R
import com.example.passionDaily.favorites.presentation.viewmodel.FavoritesViewModel
import com.example.passionDaily.quote.presentation.viewmodel.QuoteViewModel
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun BackgroundImage(imageUrl: String?) {
    imageUrl?.let {
        AsyncImage(
            model = it,
            contentDescription = "Background Image",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.5f },
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
fun LeftArrow(onClick: () -> Unit) {
    ArrowButton(onClick = onClick) {
        Image(
            painter = painterResource(id = R.drawable.left_arrow),
            contentDescription = "quote_arrow_left",
            contentScale = ContentScale.None,
            modifier = Modifier.testTag("LeftArrow")
        )
    }
}

@Composable
fun RightArrow(onClick: () -> Unit) {
    ArrowButton(onClick = onClick) {
        Image(
            painter = painterResource(id = R.drawable.right_arrow),
            contentDescription = "quote_arrow_right",
            contentScale = ContentScale.None,
            modifier = Modifier.testTag("RightArrow")
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
            contentDescription = "navigation to category screen",
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
                    text = "-$author-",
                    style = TextStyle(
                        fontSize = 22.sp,
                        lineHeight = 39.6.sp,
                        fontFamily = FontFamily(Font(R.font.yeonsung_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF8E8E8E),
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
    onRequireLogin: () -> Unit,
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
        onRequireLogin = onRequireLogin
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
        contentDescription = "share icon",
        contentScale = ContentScale.None,
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            Log.d("ShareButton", "Share button clicked")

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
    onRequireLogin: () -> Unit,
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    val coroutineScope = rememberCoroutineScope()

    val categoryId = category?.categoryId ?: return
    val userId = currentUser?.uid ?: ""

    val isFavorite by favoritesViewModel.isFavorite(userId, currentQuoteId, categoryId)
        .collectAsState(initial = false)

    var isAnimating by remember { mutableStateOf(false) }

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

    Image(
        painter = painterResource(id = iconResource),
        contentDescription = if (isFavorite) "remove from favorites icon" else "add to favorites icon",
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
                if (currentUser == null) {
                    onRequireLogin()
                } else {
                    isAnimating = true

                    if (isFavorite) {
                        Log.d(
                            "AddToFavoritesButton",
                            "Removing favorite - quoteId: $currentQuoteId, categoryId: $categoryId"
                        )
                        coroutineScope.launch {
                            favoritesViewModel.removeFavorite(currentQuoteId, categoryId)
                        }
                    } else {
                        favoritesViewModel.addFavorite(currentQuoteId)
                    }
                }
            }
    )
}

