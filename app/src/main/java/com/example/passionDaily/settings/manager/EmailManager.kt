package com.example.passionDaily.settings.manager

import android.content.Intent
import com.example.passionDaily.settings.domain.usecase.SendEmailUseCase
import javax.inject.Inject

class EmailManager @Inject constructor(
    private val sendEmailUseCase: SendEmailUseCase
) {
    fun createEmailIntent(): Intent {
        return sendEmailUseCase.createEmailIntent()
    }
}