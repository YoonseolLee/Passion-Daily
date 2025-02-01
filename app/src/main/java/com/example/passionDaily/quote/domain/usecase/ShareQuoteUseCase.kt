package com.example.passionDaily.quote.domain.usecase

import android.content.Context
import com.example.passionDaily.quote.manager.ImageShareManager
import com.example.passionDaily.quote.data.remote.RemoteQuoteRepository
import com.example.passionDaily.util.QuoteCategory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShareQuoteUseCase @Inject constructor(
    private val imageShareManager: ImageShareManager,
    private val remoteQuoteRepository: RemoteQuoteRepository
) {
    suspend fun shareQuote(
        context: Context,
        imageUrl: String?,
        quoteText: String,
        author: String
    ) = withContext(Dispatchers.Main) {
        try {
            imageShareManager.shareQuoteImage(
                context = context,
                imageUrl = imageUrl,
                quoteText = quoteText,
                author = author
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun incrementShareCount(quoteId: String, category: QuoteCategory?) {
        category?.let {
            withContext(Dispatchers.IO) {
                remoteQuoteRepository.incrementShareCount(quoteId, it)
            }
        }
    }
}
