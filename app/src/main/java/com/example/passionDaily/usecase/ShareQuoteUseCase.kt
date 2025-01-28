package com.example.passionDaily.usecase

import android.content.Context
import com.example.passionDaily.manager.ImageShareManager
import javax.inject.Inject

class ShareQuoteUseCase @Inject constructor(
    private val imageShareManager: ImageShareManager
) {
    suspend fun shareQuote(
        context: Context,
        imageUrl: String?,
        quoteText: String,
        author: String
    ) {
        imageShareManager.shareQuoteImage(
            context = context,
            imageUrl = imageUrl,
            quoteText = quoteText,
            author = author
        )
    }
}
