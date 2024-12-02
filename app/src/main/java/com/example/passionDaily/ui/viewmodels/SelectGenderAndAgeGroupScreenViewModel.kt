package com.example.passionDaily.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.util.AgeGroup
import com.example.passionDaily.util.Gender
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SelectGenderAndAgeGroupScreenViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    fun completeUserRegistration(
        pendingUserMap: Map<String, Any>?,
        gender: Gender? = null,
        ageGroup: AgeGroup? = null,
        isSkipped: Boolean = false
    ) {
        viewModelScope.launch {
            // null 체크
            val userMap = pendingUserMap?.toMutableMap() ?: return@launch

            if (!isSkipped) {
                gender?.let { userMap["gender"] = it.name }
                ageGroup?.let { userMap["ageGroup"] = it.name }
            }

            val userId = userMap["id"] as? String ?: return@launch

            try {
                val userDocRef = firestore.collection("users").document(userId)
                userDocRef.set(userMap).await()

                saveOAuthInfo(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun saveOAuthInfo(userId: String) {
        val oauthDoc = firestore.collection("users")
            .document(userId)
            .collection("oauth")
            .document("google")

        val oauthInfo = mapOf(
            "provider" to "google",
            "accessToken" to "",
            "refreshToken" to "",
            "expiresAt" to Timestamp.now()
        )

        oauthDoc.set(oauthInfo, SetOptions.merge()).await()
    }
}
