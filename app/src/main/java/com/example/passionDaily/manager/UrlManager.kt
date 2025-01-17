package com.example.passionDaily.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import javax.inject.Inject

class UrlManager @Inject constructor() {
    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}