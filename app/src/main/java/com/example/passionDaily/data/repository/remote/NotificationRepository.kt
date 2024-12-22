//package com.example.passionDaily.data.repository.remote
//
//import com.example.passionDaily.data.remote.model.Notification
//import com.google.firebase.Timestamp
//import com.google.firebase.firestore.FirebaseFirestore
//
//class NotificationRepository(private val db: FirebaseFirestore) {
//    companion object {
//        private const val COLLECTION_NOTIFICATIONS = "notifications"
//    }
//
//    suspend fun createNotification(notification: Notification) {
//        db.collection(COLLECTION_NOTIFICATIONS)
//            .add(notification)
//    }
//
//    suspend fun updateNotificationStatus(
//        notificationId: String,
//        status: Notification.NotificationStatus,
//        failReason: String? = null
//    ) {
//        val updates = mutableMapOf<String, Any>(
//            "status" to status,
//            "sentTime" to Timestamp.now()
//        )
//        failReason?.let { updates["failReason"] = it }
//
//        db.collection(COLLECTION_NOTIFICATIONS)
//            .document(notificationId)
//            .update(updates)
//    }
//}