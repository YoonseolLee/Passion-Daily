package com.example.passionDaily.favorites.usecase

import android.util.Log
import com.example.passionDaily.constants.ViewModelConstants.Favorites.TAG
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class GetRequiredDataUseCase @Inject constructor() {

    fun getRequiredDataForAdd(
        currentUser: FirebaseUser?,
        selectedCategory: QuoteCategory?,
        quotes: List<Quote>,
        quoteId: String
    ): Triple<FirebaseUser, QuoteCategory, Quote>? {
        val user = currentUser ?: run {
            Log.d(TAG, "No user logged in")
            return null
        }

        val category = selectedCategory ?: run {
            Log.d(TAG, "No category selected")
            return null
        }

        val quote = quotes.find { it.id == quoteId } ?: run {
            Log.d(TAG, "Quote not found: $quoteId")
            return null
        }
        return Triple(user, category, quote)
    }
}