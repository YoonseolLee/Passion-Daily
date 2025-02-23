package com.example.passionDaily.quote.state

import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quote.base.QuoteViewModelState
import kotlinx.coroutines.flow.StateFlow

interface QuoteBasicState : QuoteViewModelState {
    override val quotes: StateFlow<List<Quote>>
    override val currentQuote: StateFlow<Quote?>
}