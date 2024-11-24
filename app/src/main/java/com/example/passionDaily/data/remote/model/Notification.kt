package com.example.passionDaily.data.remote.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class Notification(
    val id: String = "",
    val type: NotificationType = NotificationType.QUOTE,
    val userRef: DocumentReference? = null,
    val title: String = "",
    val message: String = "",
    val status: NotificationStatus = NotificationStatus.PENDING,
    val scheduledTime: Timestamp = Timestamp.now(),
    val sentTime: Timestamp? = null,
    val failReason: String = "",
    val isDeleted: Boolean = false,
    val createdDate: Timestamp = Timestamp.now(),
    val contentRef: DocumentReference? = null
) {
    enum class NotificationType {
        QUOTE, PROMOTION
    }

    enum class NotificationStatus {
        PENDING, SENT, FAILED
    }
}
