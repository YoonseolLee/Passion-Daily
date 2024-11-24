package com.example.passionDaily.data.remote.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class Quote(
    val id: String = "",
    val text: String = "",
    val authorRef: DocumentReference? = null,
    val imageUrl: String = "",
    val categoryRef: DocumentReference? = null,
    val views: Int = 0,
    val shares: Int = 0,
    val isDeleted: Boolean = false,
    val createdDate: Timestamp = Timestamp.now(),
    val modifiedDate: Timestamp = Timestamp.now(),
    val category: CategoryInfo = CategoryInfo(),
    val author: AuthorInfo = AuthorInfo()
) {
    data class CategoryInfo(
        val id: String = "",
        val name: String = ""
    )

    data class AuthorInfo(
        val id: String = "",
        val name: String = ""
    )
}