package com.example.passionDaily.notification.data.repository.remote

import com.google.firebase.firestore.QuerySnapshot

interface UserNotificationRepository {
    suspend fun getTargetUsers(currentTime: String): QuerySnapshot
}
