package com.example.passionDaily.quote.manager

import android.content.Context
import com.example.passionDaily.quote.domain.usecase.ImageShareUseCase
import com.example.passionDaily.quote.domain.usecase.IncrementShareCountUseCase
import com.example.passionDaily.util.QuoteCategory
import javax.inject.Inject

class ShareQuoteManagerImpl @Inject constructor(
    private val imageShareUseCase: ImageShareUseCase,
    private val incrementShareCountUseCase: IncrementShareCountUseCase
) : ShareQuoteManager {
    override suspend fun shareQuote(
        context: Context,
        imageUrl: String?,
        quoteText: String,
        author: String
    ) {
        imageShareUseCase.shareQuoteImage(context, imageUrl, quoteText, author)
    }

    override suspend fun incrementShareCount(quoteId: String, category: QuoteCategory?) {
        incrementShareCountUseCase.incrementShareCount(quoteId, category)
    }
}