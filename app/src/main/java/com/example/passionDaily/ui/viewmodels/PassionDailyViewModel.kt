package com.example.passionDaily.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.entity.FavoriteEntity
import com.example.passionDaily.data.entity.NotificationEntity
import com.example.passionDaily.data.entity.QuoteCategoryEntity
import com.example.passionDaily.data.entity.QuoteEntity
import com.example.passionDaily.data.entity.TermsConsentEntity
import com.example.passionDaily.data.entity.UserEntity
import com.example.passionDaily.data.repository.PassionDailyRepository
import com.example.passionDaily.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PassionDailyViewModel
    @Inject
    constructor(
        private val repository: PassionDailyRepository,
    ) : ViewModel() {
        private val _currentUserId = MutableStateFlow<Int?>(null)
        val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

        private val _currentUser = MutableStateFlow<RequestState<UserEntity>>(RequestState.Idle)
        val currentUser: StateFlow<RequestState<UserEntity>> = _currentUser.asStateFlow()

        private val _quotes = MutableStateFlow<RequestState<List<QuoteEntity>>>(RequestState.Idle)
        val quotes: StateFlow<RequestState<List<QuoteEntity>>> = _quotes.asStateFlow()

        private val _favorites = MutableStateFlow<RequestState<List<FavoriteEntity>>>(RequestState.Idle)
        val favorites: StateFlow<RequestState<List<FavoriteEntity>>> = _favorites.asStateFlow()

        private val _categories = MutableStateFlow<RequestState<List<QuoteCategoryEntity>>>(RequestState.Idle)
        val categories: StateFlow<RequestState<List<QuoteCategoryEntity>>> = _categories.asStateFlow()

        private val _termsConsents = MutableStateFlow<RequestState<List<TermsConsentEntity>>>(RequestState.Idle)
        val termsConsents: StateFlow<RequestState<List<TermsConsentEntity>>> = _termsConsents.asStateFlow()

        private val _notificationSettings = MutableStateFlow<RequestState<NotificationEntity>>(RequestState.Idle)
        val notificationSettings: StateFlow<RequestState<NotificationEntity>> = _notificationSettings.asStateFlow()

        init {
            loadCategories()
            loadQuotes()
        }

        private fun loadQuotes() {
            viewModelScope.launch {
                _quotes.value = RequestState.Loading
                try {
                    repository.getAllQuotes().collect { quotes ->
                        _quotes.value = RequestState.Success(quotes)
                    }
                } catch (e: Exception) {
                    _quotes.value = RequestState.Error(e)
                }
            }
        }

        fun setCurrentUser(userId: Int) {
            _currentUserId.value = userId
            loadUserData(userId)
            loadUserFavorites(userId)
            loadTermsConsents(userId)
            loadNotificationSettings(userId)
        }

        private fun loadUserData(userId: Int) {
            viewModelScope.launch {
                _currentUser.value = RequestState.Loading
                try {
                    repository.getUserById(userId).collect { user ->
                        _currentUser.value = user?.let { RequestState.Success(it) }
                            ?: RequestState.Error(Exception("User not found"))
                    }
                } catch (e: Exception) {
                    _currentUser.value = RequestState.Error(e)
                }
            }
        }

        private fun loadUserFavorites(userId: Int) {
            viewModelScope.launch {
                _favorites.value = RequestState.Loading
                try {
                    repository.getFavoritesByUserId(userId).collect { favoritesList ->
                        _favorites.value = RequestState.Success(favoritesList)
                    }
                } catch (e: Exception) {
                    _favorites.value = RequestState.Error(e)
                }
            }
        }

        private fun loadTermsConsents(userId: Int) {
            viewModelScope.launch {
                _termsConsents.value = RequestState.Loading
                try {
                    repository.getTermsConsentsByUserId(userId).collect { consentsList ->
                        _termsConsents.value = RequestState.Success(consentsList)
                    }
                } catch (e: Exception) {
                    _termsConsents.value = RequestState.Error(e)
                }
            }
        }

        private fun loadNotificationSettings(userId: Int) {
            viewModelScope.launch {
                _notificationSettings.value = RequestState.Loading
                try {
                    val settings = repository.getNotificationSettings(userId)
                    _notificationSettings.value = settings?.let { RequestState.Success(it) }
                        ?: RequestState.Error(Exception("Notification settings not found"))
                } catch (e: Exception) {
                    _notificationSettings.value = RequestState.Error(e)
                }
            }
        }

        private fun loadCategories() {
            viewModelScope.launch {
                _categories.value = RequestState.Loading
                try {
                    val categoriesList = repository.getAllCategories()
                    _categories.value = RequestState.Success(categoriesList)
                } catch (e: Exception) {
                    _categories.value = RequestState.Error(e)
                }
            }
        }

        fun addToFavorites(quoteId: Int) {
            viewModelScope.launch {
                currentUserId.value?.let { userId ->
                    _favorites.value = RequestState.Loading
                    try {
                        val favorite =
                            FavoriteEntity(
                                userId = userId,
                                quoteId = quoteId,
                                createdDate = Date(),
                            )
                        repository.addFavorite(favorite)
                        // Refresh favorites list after adding
                        loadUserFavorites(userId)
                    } catch (e: Exception) {
                        _favorites.value = RequestState.Error(e)
                    }
                }
            }
        }

        fun removeFromFavorites(favorite: FavoriteEntity) {
            viewModelScope.launch {
                _favorites.value = RequestState.Loading
                try {
                    repository.deleteFavorite(favorite)
                    // Refresh favorites list after removal
                    currentUserId.value?.let { loadUserFavorites(it) }
                } catch (e: Exception) {
                    _favorites.value = RequestState.Error(e)
                }
            }
        }

        fun updateNotificationSettings(settings: NotificationEntity) {
            viewModelScope.launch {
                _notificationSettings.value = RequestState.Loading
                try {
                    repository.updateNotificationSettings(settings)
                    _notificationSettings.value = RequestState.Success(settings)
                } catch (e: Exception) {
                    _notificationSettings.value = RequestState.Error(e)
                }
            }
        }

        fun addTermsConsent(termsConsent: TermsConsentEntity) {
            viewModelScope.launch {
                _termsConsents.value = RequestState.Loading
                try {
                    repository.insertTermsConsent(termsConsent)
                    // Refresh terms consents after adding
                    currentUserId.value?.let { loadTermsConsents(it) }
                } catch (e: Exception) {
                    _termsConsents.value = RequestState.Error(e)
                }
            }
        }
    }
