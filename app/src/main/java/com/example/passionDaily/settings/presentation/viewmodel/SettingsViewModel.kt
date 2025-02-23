package com.example.passionDaily.settings.presentation.viewmodel

import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import com.example.passionDaily.settings.base.SettingsViewModelActions
import com.example.passionDaily.settings.manager.EmailManager
import com.example.passionDaily.toast.manager.ToastManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
            is IOException -> {
                toastManager.showNetworkErrorToast()
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