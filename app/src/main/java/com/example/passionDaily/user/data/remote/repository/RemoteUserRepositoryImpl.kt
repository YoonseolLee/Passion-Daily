package com.example.passionDaily.user.data.remote.repository

import android.util.Log
import com.example.passionDaily.R
import com.example.passionDaily.constants.RepositoryConstants.RemoteUser.TAG
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.user.data.remote.model.User
import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.util.TimeUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

class RemoteUserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val localUserRepository: LocalUserRepository,
    private val timeUtil: TimeUtil,
    private val stringProvider: StringProvider
) : RemoteUserRepository {

    override suspend fun isUserRegistered(userId: String): Boolean {
        return try {
            val documentSnapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            documentSnapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateLastSyncDate(userId: String) {
        try {
            val now = timeUtil.getCurrentTimestamp()
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "lastSyncDate" to now,
                        "lastLoginDate" to now
                    )
                ).await()

            Log.d("UserSync", "Successfully updated lastSyncDate and lastLoginDate: $now")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification time in Firestore", e)
            when {
                e is UnknownHostException ||
                        e.cause is UnknownHostException ||
                        e is TimeoutCancellationException ->
                    throw IOException("Network error while accessing Firestore", e)
                else -> throw e
            }
        }
    }

    override suspend fun syncFirestoreUserToRoom(userId: String) {
        try {
            val firestoreUser = fetchFirestoreUser(userId)
            val userEntity = localUserRepository.convertToUserEntity(firestoreUser)
            localUserRepository.saveUser(userEntity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification time in Firestore", e)
            when {
                e is UnknownHostException ||
                        e.cause is UnknownHostException ||
                        e is TimeoutCancellationException ->
                    throw IOException("Network error while accessing Firestore", e)
                else -> throw e
            }
        }
    }

    override suspend fun fetchFirestoreUser(userId: String): User {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            userDoc.toObject(User::class.java)
                ?: throw IllegalStateException()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification time in Firestore", e)
            when {
                e is UnknownHostException ||
                        e.cause is UnknownHostException ||
                        e is TimeoutCancellationException ->
                    throw IOException("Network error while accessing Firestore", e)
                else -> throw e
            }
        }
    }

    override suspend fun addUserProfile(userId: String, profileMap: Map<String, Any?>) {
        try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (!userDoc.exists()) {
                firestore.collection("users")
                    .document(userId)
                    .set(profileMap)
                    .await()

                updateFCMToken(userId)

                Log.d("Firestore", "User profile added successfully: $userId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification time in Firestore", e)
            when {
                e is UnknownHostException ||
                        e.cause is UnknownHostException ||
                        e is TimeoutCancellationException ->
                    throw IOException("Network error while accessing Firestore", e)
                else -> throw e
            }
        }
    }

    private fun updateFCMToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                firestore.collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("Firestore", "FCM Token updated successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error updating FCM token", e)
                    }
            } else {
                Log.e("Firestore", "Failed to get FCM token", task.exception)
            }
        }
    }

    override suspend fun updateNotificationSettingsToFirestore(
        userId: String,
        enabled: Boolean
    ): Unit = withContext(Dispatchers.IO) {
        try {
            withTimeout(3000L) {
                firestore.collection(stringProvider.getString(R.string.users))
                    .document(userId)
                    .update("notificationEnabled", enabled)
                    .await()

                Log.d(TAG, "Successfully updated notification settings to Firestore")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification settings in Firestore", e)
            when {
                e is UnknownHostException ||
                        e.cause is UnknownHostException ||
                        e is TimeoutCancellationException ->
                    throw IOException("Network error while accessing Firestore", e)
                else -> throw e
            }
        }
    }

    override suspend fun updateNotificationTimeToFirestore(
        userId: String,
        newTime: String
    ): Unit = withContext(Dispatchers.IO) {
        try {
            withTimeout(3000L) {
                firestore.collection(stringProvider.getString(R.string.users))
                    .document(userId)
                    .update("notificationTime", newTime)
                    .await()

                Log.d(TAG, "Successfully updated notification time to Firestore")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification time in Firestore", e)
            when {
                e is UnknownHostException ||
                        e.cause is UnknownHostException ||
                        e is TimeoutCancellationException ->
                    throw IOException("Network error while accessing Firestore", e)
                else -> throw e
            }
        }
    }

    override suspend fun deleteUserDataFromFirestore(userId: String): Unit = withContext(Dispatchers.IO) {
        try {
            withTimeout(3000L) {
                firestore.collection(stringProvider.getString(R.string.users))
                    .document(userId)
                    .delete()
                    .await()

                Log.d(TAG, "Successfully deleted user from Firestore")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user from Firestore", e)
            when {
                e is UnknownHostException ||
                        e.cause is UnknownHostException ||
                        e is TimeoutCancellationException ->
                    throw IOException("Network error while accessing Firestore", e)
                else -> throw e
            }
        }
    }

    override suspend fun deleteFavoritesFromFirestore(userId: String): Unit = withContext(Dispatchers.IO) {
        try {
            withTimeout(3000L) {
                val savedQuotesRef = firestore
                    .collection(stringProvider.getString(R.string.favorites_eng))
                    .document(userId)
                    .collection("saved_quotes")

                val savedQuotes = savedQuotesRef.get().await()

                Log.d(TAG, "Found ${savedQuotes.size()} documents in saved_quotes")

                savedQuotes.documents.forEachIndexed { index, document ->
                    Log.d(TAG, "Attempting to delete document ${index + 1}/${savedQuotes.size()}: ${document.id}")
                    try {
                        savedQuotesRef.document(document.id).delete().await()
                        Log.d(TAG, "Successfully deleted document: ${document.id}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to delete document: ${document.id}", e)
                    }
                }

                firestore.collection(stringProvider.getString(R.string.favorites_eng))
                    .document(userId)
                    .delete()
                    .await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete favorites from Firestore", e)
            when {
                e is UnknownHostException ||
                        e.cause is UnknownHostException ||
                        e is TimeoutCancellationException -> {
                    Log.e(TAG, "Network or timeout error occurred", e)
                    throw IOException("Network error while accessing Firestore", e)
                }
                else -> {
                    Log.e(TAG, "Unexpected error occurred", e)
                    throw e
                }
            }
        }
    }
}