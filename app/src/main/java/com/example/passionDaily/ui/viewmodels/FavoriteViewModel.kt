package com.example.passionDaily.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.local.entity.FavoriteEntity
import com.example.passionDaily.data.repository.local.FavoriteRepository
import com.example.passionDaily.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    private val _favorites = MutableStateFlow<RequestState<List<FavoriteEntity>>>(RequestState.Idle)
    val favorites: StateFlow<RequestState<List<FavoriteEntity>>> = _favorites

    private val _selectedFavorite = MutableStateFlow<FavoriteEntity?>(null)
    val selectedFavorite: StateFlow<FavoriteEntity?> = _selectedFavorite

    fun getFavoriteQuotes(userId: Int) {
        _favorites.value = RequestState.Loading
        viewModelScope.launch {
            try {
                favoriteRepository.getFavoritesByUserId(userId).collect { favorites ->
                    _favorites.value = RequestState.Success(favorites)
                }
            } catch (e: Exception) {
                _favorites.value = RequestState.Error(e)
            }
        }
    }

    /**
     * 별표가 색칠되어 있는 경우 (즐겨찾기에 이미 추가된 경우):
     * 사용자가 색칠된 별표를 클릭하면, isFavorite 값이 true로 판단되고, 그에 따라 favoriteRepository.deleteFavorite(userId, quoteId) 함수가 호출됩니다.
     * 이 함수는 즐겨찾기 목록에서 해당 명언을 삭제합니다.
     *
     * 별표가 비어있는 경우 (즐겨찾기에 추가되지 않은 경우):
     * 사용자가 빈 별표를 클릭하면, isFavorite 값이 false로 판단되고, 그에 따라 favoriteRepository.insertFavorite(FavoriteEntity(userId, quoteId, createdDate)) 함수가 호출됩니다.
     * 이 함수는 해당 명언을 즐겨찾기 목록에 추가합니다.
     */

    fun toggleFavorites(userId: Int, quoteId: Int, createdDate: Long, isSynced: Boolean) {
        viewModelScope.launch {
            val isFavorite = favoriteRepository.isFavorite(userId, quoteId)
            if (isFavorite) {
                favoriteRepository.deleteFavorite(userId, quoteId)
            } else {
                favoriteRepository.insertFavorite(
                    FavoriteEntity(
                        userId = userId,
                        quoteId = quoteId,
                        createdDate = createdDate,
                        isSynced = isSynced,
                    )
                )
            }
        }
    }

    suspend fun removeFavorite(userId: Int, quoteId: Int) {
        viewModelScope.launch {
            try {
                favoriteRepository.deleteFavorite(userId, quoteId)
                // 삭제 후 목록 갱신
                getFavoriteQuotes(userId)
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }
}
