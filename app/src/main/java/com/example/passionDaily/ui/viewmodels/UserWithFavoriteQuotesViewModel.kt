package com.example.passionDaily.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.local.relation.UserWithFavoriteQuotes
import com.example.passionDaily.data.repository.local.QuoteRepository
import com.example.passionDaily.data.repository.local.UserRepository
import com.example.passionDaily.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserWithFavoriteQuotesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val quoteRepository: QuoteRepository
) : ViewModel() {
    private val _userWithFavoriteQuotes = MutableStateFlow<RequestState<UserWithFavoriteQuotes>>(RequestState.Idle)
    val userWithFavoriteQuotes: StateFlow<RequestState<UserWithFavoriteQuotes>> = _userWithFavoriteQuotes

    fun getUserFavoriteQuotes(userId: Int) {
        _userWithFavoriteQuotes.value = RequestState.Loading
        viewModelScope.launch {
            try {
                val userWithFavorites = userRepository.getUserWithFavorites(userId)

                if (userWithFavorites != null) {

                    // FavoriteEntity의 quote_id를 사용하여 QuoteEntity들을 조회
                    val quoteIds = userWithFavorites.favorites.map { it.quoteId }
                    val quotes = quoteRepository.getQuotesByIds(quoteIds)

                    val userWithFavoriteQuotes = UserWithFavoriteQuotes(
                        user = userWithFavorites.user,
                        favoriteQuotes = quotes
                    )

                    _userWithFavoriteQuotes.value = RequestState.Success(userWithFavoriteQuotes)
                } else {
                    _userWithFavoriteQuotes.value = RequestState.Error(Exception("User not found"))
                }
            } catch (e: Exception) {
                _userWithFavoriteQuotes.value = RequestState.Error(e)
            }
        }
    }
}
