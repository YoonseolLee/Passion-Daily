package com.example.passionDaily.data.repository.remote

import android.util.Log
import com.example.passionDaily.data.remote.model.User
import com.example.passionDaily.data.repository.local.LocalUserRepository
import com.example.passionDaily.util.TimeUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RemoteUserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val localUserRepository: LocalUserRepository,
    private val timeUtil: TimeUtil
) : RemoteUserRepository {

    override suspend fun isUserRegistered(userId: String): Boolean {
        return try {
            val documentSnapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            documentSnapshot.exists()
        } catch (e: FirebaseFirestoreException) {
            Log.e("Firestore", "Firestore exception occurred: ${e.message}", e)
            false
        } catch (e: Exception) {
            Log.e("Firestore", "Error checking user registration: ${e.message}", e)
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
        } catch (e: FirebaseFirestoreException) {
            Log.e("UserSync", "Firestore exception while updating dates: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e("UserSync", "Failed to update dates in Firestore: ${e.message}", e)
            throw e
        }
    }

    override suspend fun syncFirestoreUserToRoom(userId: String) {
        try {
            val firestoreUser = fetchFirestoreUser(userId)
            val userEntity = localUserRepository.convertToUserEntity(firestoreUser)
            localUserRepository.saveUser(userEntity)
        } catch (e: FirebaseFirestoreException) {
            Log.e("UserSync", "Firestore exception during sync: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e("UserSync", "Failed to sync user data: ${e.message}", e)
            throw e
        }
    }

    override suspend fun fetchFirestoreUser(userId: String): User {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            userDoc.toObject(User::class.java)
                ?: throw IllegalStateException("User data not found in Firestore for ID: $userId")
        } catch (e: FirebaseFirestoreException) {
            Log.e("Firestore", "Firestore exception while fetching user: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching user data: ${e.message}", e)
            throw e
        }
    }
}