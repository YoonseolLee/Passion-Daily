package com.example.passionDaily.usecase

import com.example.passionDaily.data.repository.remote.RemoteQuoteRepository
import com.example.passionDaily.util.QuoteCategory
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