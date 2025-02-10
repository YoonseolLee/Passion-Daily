package com.example.passionDaily.login.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import javax.inject.Inject

class UrlManagerImpl @Inject constructor() : UrlManager {
    override fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}