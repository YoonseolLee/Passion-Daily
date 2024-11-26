package com.example.passionDaily.data.remote.model.user

import com.google.firebase.Timestamp

data class UserDevice(
    val token: String = "", // device_token
    val osInfo: OsInfo = OsInfo(),
    val appVersion: String = "",
    val deviceName: String = "",
    val lastAccessedDate: Timestamp = Timestamp.now(),
) {
    data class OsInfo(
        val osType: String = "",
        val osVersion: String = ""
    )
}