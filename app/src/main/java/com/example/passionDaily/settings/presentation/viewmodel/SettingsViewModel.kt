package com.example.passionDaily.settings.presentation.viewmodel

import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.settings.base.SettingsViewModelActions
import com.example.passionDaily.settings.manager.EmailManager
import com.example.passionDaily.settings.stateholder.SettingsStateHolder
import com.example.passionDaily.toast.manager.ToastManager
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import java.io.IOException
import java.net.URISyntaxException
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val toastManager: ToastManager,
    private val emailManager: EmailManager,
) : ViewModel(), SettingsViewModelActions {

    override fun createEmailIntent(): Intent? {
        return try {
            emailManager.createEmailIntent()
        } catch (e: URISyntaxException) {
            toastManager.showURISyntaxException()
            null
        } catch (e: Exception) {
            handleError(e)
            null
        }
    }

    private fun handleError(e: Exception) {
        when (e) {
            is IOException, is FirebaseNetworkException -> {
                toastManager.showNetworkErrorToast()
            }

            is FirebaseFirestoreException -> {
                toastManager.showFirebaseErrorToast()
            }

            is SQLiteException -> {
                toastManager.showRoomDatabaseErrorToast()
            }

            is IllegalStateException -> {
                toastManager.showGeneralErrorToast()
            }

            else -> {
                toastManager.showGeneralErrorToast()
            }
        }
    }
}