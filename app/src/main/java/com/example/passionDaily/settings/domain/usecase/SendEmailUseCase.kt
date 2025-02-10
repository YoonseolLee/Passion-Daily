package com.example.passionDaily.settings.domain.usecase

import android.content.Intent
import android.net.Uri
import com.example.passionDaily.R
import com.example.passionDaily.resources.StringProvider
import javax.inject.Inject

class SendEmailUseCase @Inject constructor(
    private val stringProvider: StringProvider,
) {
    fun createEmailIntent(): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse(stringProvider.getString(R.string.email_address))
        }
    }
}