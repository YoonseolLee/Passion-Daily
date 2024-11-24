package com.example.passionDaily.data.repository.remote

import com.example.passionDaily.data.remote.model.user.User
import com.example.passionDaily.data.remote.model.user.UserDevice
import com.example.passionDaily.data.remote.model.user.UserOauth
import com.example.passionDaily.data.remote.model.user.UserTerm
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_USERS = "users"
        private const val SUBCOLLECTION_TERMS = "terms"
        private const val SUBCOLLECTION_DEVICES = "devices"
        private const val SUBCOLLECTION_OAUTH = "oauth"
    }

    // User CRUD operations
    suspend fun createUser(user: User) {
        db.collection(COLLECTION_USERS)
            .document(user.id)
            .set(user)
    }

    suspend fun getUser(userId: String): User? {
        return db.collection(COLLECTION_USERS)
            .document(userId)
            .get()
            .await()
            .toObject(User::class.java)
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>) {
        db.collection(COLLECTION_USERS)
            .document(userId)
            .update(updates)
    }

    // Terms operations
    suspend fun addUserTerm(userId: String, term: UserTerm) {
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(SUBCOLLECTION_TERMS)
            .document(term.id)
            .set(term)
    }

    suspend fun getUserTerms(userId: String): List<UserTerm> {
        return db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(SUBCOLLECTION_TERMS)
            .get()
            .await()
            .toObjects(UserTerm::class.java)
    }

    // Device operations
    suspend fun addUserDevice(userId: String, device: UserDevice) {
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(SUBCOLLECTION_DEVICES)
            .document(device.token)
            .set(device)
    }

    suspend fun getUserDevices(userId: String): List<UserDevice> {
        return db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(SUBCOLLECTION_DEVICES)
            .get()
            .await()
            .toObjects(UserDevice::class.java)
    }

    suspend fun removeUserDevice(userId: String, deviceToken: String) {
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(SUBCOLLECTION_DEVICES)
            .document(deviceToken)
            .delete()
    }

    // OAuth operations
    suspend fun addUserOauth(userId: String, oauth: UserOauth) {
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(SUBCOLLECTION_OAUTH)
            .document(oauth.provider)
            .set(oauth)
    }

    suspend fun getUserOauth(userId: String, provider: String): UserOauth? {
        return db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(SUBCOLLECTION_OAUTH)
            .document(provider)
            .get()
            .await()
            .toObject(UserOauth::class.java)
    }

    suspend fun removeUserOauth(userId: String, provider: String) {
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(SUBCOLLECTION_OAUTH)
            .document(provider)
            .delete()
    }
}