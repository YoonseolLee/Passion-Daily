package com.example.passionDaily.quote.domain.usecase

import com.example.passionDaily.quote.data.remote.repository.RemoteQuoteRepository
import com.example.passionDaily.quotecategory.model.QuoteCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IncrementShareCountUseCase @Inject constructor(
    private val remoteQuoteRepository: RemoteQuoteRepository
) {
    suspend fun incrementShareCount(quoteId: String, category: QuoteCategory?) {
        category?.let {
            withContext(Dispatchers.IO) {
                remoteQuoteRepository.incrementShareCount(quoteId, it)
            }
        }
    }
}