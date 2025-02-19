package com.example.passionDaily.user.data.remote.repository

import com.example.passionDaily.R
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.user.data.local.entity.UserEntity
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

        } catch (e: Exception) {
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
            // Firestore에서 최신 사용자 데이터 가져오기
            val firestoreUser = fetchFirestoreUser(userId)

            // 로컬 DB에서 현재 사용자 데이터 가져오기
            val localUser = localUserRepository.getUserById(userId)

            if (localUser == null) {
                // 로컬 DB에 사용자가 없으면 새로 생성
                val userEntity = localUserRepository.convertToUserEntity(firestoreUser)
                localUserRepository.saveUser(userEntity)
            } else {
                // 로컬 DB에 사용자가 있으면 필드별로 비교 후 업데이트
                val needsUpdate = checkIfUpdateNeeded(firestoreUser, localUser)

                if (needsUpdate) {
                    val updatedEntity = localUserRepository.convertToUserEntity(firestoreUser)
                    localUserRepository.upsertUser(updatedEntity)
                }
            }

            // 마지막 동기화 일자 업데이트
            updateLastSyncDate(userId)

        } catch (e: Exception) {
            when {
                e is UnknownHostException ||
                        e.cause is UnknownHostException ||
                        e is TimeoutCancellationException ->
                    throw IOException("Network error while accessing Firestore", e)
                else -> throw e
            }
        }
    }

    private fun checkIfUpdateNeeded(firestoreUser: User, localUser: UserEntity): Boolean {
        return firestoreUser.id != localUser.userId ||
                firestoreUser.name != localUser.name ||
                firestoreUser.notificationEnabled != localUser.notificationEnabled ||
                firestoreUser.notificationTime != localUser.notificationTime ||
                timeUtil.parseTimestamp(firestoreUser.lastSyncDate) != localUser.lastSyncDate
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

            }
        } catch (e: Exception) {
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
                    }
                    .addOnFailureListener { e ->
                    }
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

            }
        } catch (e: Exception) {
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

            }
        } catch (e: Exception) {
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
            }
        } catch (e: Exception) {
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


                savedQuotes.documents.forEachIndexed { index, document ->
                        savedQuotesRef.document(document.id).delete().await()
                }

                firestore.collection(stringProvider.getString(R.string.favorites_eng))
                    .document(userId)
                    .delete()
                    .await()
            }
        } catch (e: Exception) {
            when {
                e is UnknownHostException ||
                        e.cause is UnknownHostException ||
                        e is TimeoutCancellationException -> {
                    throw IOException("Network error while accessing Firestore", e)
                }
                else -> {
                    throw e
                }
            }
        }
    }
}