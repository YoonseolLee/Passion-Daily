package com.example.passionDaily.login.action

import android.content.Context
import com.example.passionDaily.login.base.SharedLogInActions

interface UrlActions : SharedLogInActions {
    override fun openUrl(context: Context, url: String)
}