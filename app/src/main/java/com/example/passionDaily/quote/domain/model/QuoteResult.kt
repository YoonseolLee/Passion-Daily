package com.example.passionDaily.quote.domain.model

import com.example.passionDaily.quote.data.remote.model.Quote
import com.google.firebase.firestore.DocumentSnapshot

data class QuoteResult(
    val quotes: List<Quote>,
    val lastDocument: DocumentSnapshot?
)
