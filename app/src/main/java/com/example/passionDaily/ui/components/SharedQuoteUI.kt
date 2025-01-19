package com.example.passionDaily.ui.components

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.manager.ImageShareManager
import com.example.passionDaily.ui.screens.ShareableQuoteImage
import com.example.passionDaily.ui.viewmodels.FavoritesViewModel
import com.example.passionDaily.ui.viewmodels.QuoteViewModel
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.auth.FirebaseAuth

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
fun LeftArrow() {
    Image(
        painter = painterResource(id = R.drawable.quote_arrow_left),
        contentDescription = "quote_arrow_left",
        contentScale = ContentScale.None
    )
}

@Composable
fun RightArrow() {
    Image(
        painter = painterResource(id = R.drawable.quote_arrow_right),
        contentDescription = "quote_arrow_left",
        contentScale = ContentScale.None
    )
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
            .clickable { onCategoryClicked() }
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
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(300.dp)
                .wrapContentHeight()
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
        }

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
            modifier = Modifier.width(300.dp)
        )
    }
}

@Composable
fun Buttons(
    quoteViewModel: QuoteViewModel,
    favoritesViewModel: FavoritesViewModel,
    currentQuoteId: String,
    category: QuoteCategory? = null,
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
    category: QuoteCategory? = null,
    quoteDisplay: QuoteDisplay
) {
    val context = LocalContext.current

    Image(
        painter = painterResource(id = R.drawable.share_icon),
        contentDescription = "share icon",
        contentScale = ContentScale.None,
        modifier = Modifier.clickable {
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
    category: QuoteCategory?,
    onRequireLogin: () -> Unit,
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    val categoryId = category?.categoryId ?: 0
    val userId = currentUser?.uid ?: ""

    val isFavorite by favoritesViewModel.isFavorite(userId, currentQuoteId, categoryId)
        .collectAsState(initial = false)

    val iconResource = if (isFavorite) {
        R.drawable.remove_from_favorites_icon
    } else {
        R.drawable.add_to_favorites_icon
    }

    Image(
        painter = painterResource(id = iconResource),
        contentDescription = if (isFavorite) "remove from favorites icon" else "add to favorites icon",
        contentScale = ContentScale.None,
        modifier = Modifier.clickable {
            if (currentUser == null) {
                Toast.makeText(
                    context,
                    "즐겨찾기 기능을 사용하려면 로그인이 필요합니다.",
                    Toast.LENGTH_SHORT
                ).show()
                onRequireLogin()
            } else {
                if (isFavorite) {
                    Log.d(
                        "AddToFavoritesButton",
                        "Removing favorite - quoteId: $currentQuoteId, categoryId: $categoryId"
                    )
                    favoritesViewModel.removeFavorite(currentQuoteId, categoryId)
                } else {
                    favoritesViewModel.addFavorite(currentQuoteId)
                }
            }
        }
    )
}