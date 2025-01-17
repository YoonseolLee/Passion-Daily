package com.example.passionDaily.domain.usecase

import android.content.Context
import android.content.Intent
import javax.inject.Inject

class QuoteUseCase @Inject constructor(
) {

    // TODO: 이미지 공유로 변경
    fun shareText(context:Context, text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val chooser = Intent.createChooser(intent, "공유하기")
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}