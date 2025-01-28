package com.example.passionDaily.usecase

import android.content.Context
import com.example.passionDaily.manager.ImageShareManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShareQuoteUseCase @Inject constructor(
    private val imageShareManager: ImageShareManager,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend fun shareQuote(
        context: Context,
        imageUrl: String?,
        quoteText: String,
        author: String
    ) = withContext(defaultDispatcher) {
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
}
