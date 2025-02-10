package com.example.passionDaily.settings.manager

import android.content.Intent
import com.example.passionDaily.settings.domain.usecase.SendEmailUseCase
import javax.inject.Inject

class EmailManagerImpl @Inject constructor(
    private val sendEmailUseCase: SendEmailUseCase
) : EmailManager {
    override fun createEmailIntent(): Intent {
        return sendEmailUseCase.createEmailIntent()
    }
}
