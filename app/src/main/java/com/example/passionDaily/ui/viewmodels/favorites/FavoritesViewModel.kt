package com.example.passionDaily.ui.viewmodels.favorites

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passionDaily.data.entity.FavoriteEntity
import com.example.passionDaily.data.repository.PassionDailyRepository
import com.example.passionDaily.ui.viewmodels.base.BaseViewModel
import com.example.passionDaily.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: PassionDailyRepository
) : BaseViewModel() {
    private val _favorites = MutableStateFlow<RequestState<List<FavoriteEntity>>>(RequestState.Idle)
    val favorites: StateFlow<RequestState<List<FavoriteEntity>>> = _favorites.asStateFlow()

    fun loadFavorites(userId: Int) {
        viewModelScope.launch {
            startLoading()
            _favorites.value = RequestState.Loading
            try {
                repository.getFavoritesByUserId(userId).collect { favoritesList ->
                    _favorites.value = RequestState.Success(favoritesList)
                    stopLoading()
                }
            } catch (e: Exception) {
                _favorites.value = RequestState.Error(e)
                stopLoading()
            }
        }
    }

    fun addToFavorites(userId: Int, quoteId: Int) {
        viewModelScope.launch {
            startLoading()
            try {
                val favorite = FavoriteEntity(
                    userId = userId,
                    quoteId = quoteId,
                    createdDate = System.currentTimeMillis()
                )
                repository.addFavorite(favorite)
                loadFavorites(userId)  // 목록 새로고침
            } catch (e: Exception) {
                _favorites.value = RequestState.Error(e)
                stopLoading()
            }
        }
    }

    fun removeFromFavorites(userId: Int, favorite: FavoriteEntity) {
        viewModelScope.launch {
            startLoading()
            try {
                repository.deleteFavorite(favorite)
                loadFavorites(userId)  // 목록 새로고침
            } catch (e: Exception) {
                _favorites.value = RequestState.Error(e)
                stopLoading()
            }
        }
    }
}
