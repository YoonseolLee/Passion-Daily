package com.example.passionDaily.notification.data.repository.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserNotificationRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun getTargetUsers(currentTime: String): QuerySnapshot {
        return db.collection("users")
            .whereEqualTo("notificationEnabled", true)
            .whereEqualTo("notificationTime", currentTime)
            .get()
            .await()
    }
}